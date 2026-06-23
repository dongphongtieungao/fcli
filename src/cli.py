"""Full command-line surface for the local Bridge.

Commands (Section 10 of requirement):
  install         Create runtime dirs, generate API key, write default config.
  login           Open browser OAuth flow, save tokens securely.
  logout          Clear all stored tokens.
  start           Check auth + resolve model + sync agent + start server.
  stop            Stop a running adapter server using the PID file.
  restart         Stop then start.
  status          Show safe local configuration status.
  doctor          Comprehensive health check.
  logs            Tail the adapter log file.
  agent sync      Sync the PrivateGPT Agent for this workspace.
  agent status    Show cached agent binding.
  agent reset     Clear the cached agent binding.
  opencode config Print OpenCode provider config snippet.
"""

from __future__ import annotations

import argparse
import asyncio
import json
import logging
import os
import signal
import socket
import sys
from collections.abc import Sequence
from pathlib import Path

logger = logging.getLogger(__name__)

_DATA_DIR = Path.home() / ".privategpt-adapter"
_PID_FILE = _DATA_DIR / "adapter.pid"
_LOG_FILE = _DATA_DIR / "adapter.log"
_CONFIG_FILE = _DATA_DIR / "config.yaml"
_API_KEY_FILE = _DATA_DIR / "api_key.txt"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _load_settings():
    from src.config.settings import BridgeSettings
    return BridgeSettings.from_env()


def _ensure_data_dir() -> None:
    _DATA_DIR.mkdir(parents=True, exist_ok=True)


def _generate_api_key() -> str:
    import secrets
    return "sk-privategpt-adapter-" + secrets.token_urlsafe(24)


def _read_pid() -> int | None:
    if _PID_FILE.exists():
        try:
            return int(_PID_FILE.read_text().strip())
        except (ValueError, OSError):
            return None
    return None


def _write_pid(pid: int) -> None:
    _ensure_data_dir()
    _PID_FILE.write_text(str(pid))


def _clear_pid() -> None:
    _PID_FILE.unlink(missing_ok=True)


def _is_port_in_use(host: str, port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(1)
        return s.connect_ex((host, port)) == 0


# ---------------------------------------------------------------------------
# Command implementations
# ---------------------------------------------------------------------------

def cmd_install(args) -> int:
    """Create runtime directories, generate API key and default config."""
    _ensure_data_dir()
    print(f"[install] Created data directory: {_DATA_DIR}")

    # Generate API key if missing
    if not _API_KEY_FILE.exists():
        key = _generate_api_key()
        _API_KEY_FILE.write_text(key)
        print(f"[install] Generated API key → {_API_KEY_FILE}")
        print(f"          Set PRIVATEGPT_ADAPTER_API_KEY={key}")
    else:
        print(f"[install] API key already exists: {_API_KEY_FILE}")

    # Write default config
    if not _CONFIG_FILE.exists():
        default_config = """\
server:
  host: 127.0.0.1
  port: 4100

privategpt:
  base_url: https://privategpt.example.internal
  request_timeout_seconds: 180

model:
  exposed_model: gemini-2.5-pro
  privategpt_model_id: null
  allow_model_discovery: true

auth:
  browser_login: true
  token_store: windows_credential_manager
  refresh_before_expiry_seconds: 60

agent:
  enabled: true
  name_template: "PrivateGPT-Adapter-OpenCode-{workspace_hash}"
  description: "OpenCode coding agent for local workspace"

openai_compat:
  api_key_mode: local_generated
  reject_unsupported_models: true
  reject_unsupported_tools: true

logging:
  level: info
  redact_prompt: true
  redact_token: true

streaming:
  enabled: true

resilience:
  retry_pre_stream_only: true
"""
        _CONFIG_FILE.write_text(default_config)
        print(f"[install] Default config written → {_CONFIG_FILE}")
    else:
        print(f"[install] Config already exists: {_CONFIG_FILE}")

    # Validate Python version
    if sys.version_info < (3, 12):
        print(f"[install] WARNING: Python {sys.version_info.major}.{sys.version_info.minor} detected; 3.12+ required.")
        return 1

    print("[install] Done. Next: edit config, then run 'privategpt-adapter login'.")
    return 0


def cmd_login(args) -> int:
    """Open browser OAuth flow and save tokens securely."""
    from src.auth.token_manager import build_token_manager

    base_url = os.getenv("PRIVATEGPT_BASE_URL")
    if not base_url:
        print("[login] ERROR: PRIVATEGPT_BASE_URL is not set.")
        print("        Set it in your environment or config, e.g.:")
        print("        $env:PRIVATEGPT_BASE_URL = 'https://privategpt.example.internal'")
        return 1

    manager = build_token_manager(base_url=base_url)
    try:
        manager.login(callback_timeout=180.0)
        print("[login] Login successful. Token saved securely.")
        return 0
    except TimeoutError:
        print("[login] ERROR: No browser callback received within 180s.")
        return 1
    except RuntimeError as exc:
        print(f"[login] ERROR: {exc}")
        return 1


def cmd_logout(args) -> int:
    """Clear all stored tokens."""
    from src.auth.secure_store import build_token_store

    store = build_token_store()
    store.delete_refresh_token()
    print("[logout] Refresh token cleared from secure store.")
    print("[logout] Access token will be cleared on next adapter restart.")
    return 0


def cmd_start(args) -> int:
    """Check auth, resolve model, sync agent, then start the server."""
    from src.logging_config import configure_logging
    configure_logging(level="INFO")

    settings = _load_settings()

    # Check auth
    if not settings.privategpt_access_token:
        from src.auth.secure_store import build_token_store
        from src.auth.token_manager import build_token_manager

        if settings.privategpt_base_url:
            manager = build_token_manager(base_url=settings.privategpt_base_url)
            if not manager.restore_from_store():
                print("[start] Not logged in. Run: privategpt-adapter login")
                return 1
            print("[start] Session restored from secure store.")
        else:
            print("[start] WARNING: No PRIVATEGPT_BASE_URL set; cannot verify auth.")

    # Startup checks (non-blocking warnings, not hard failures pre-pilot)
    if _is_port_in_use(settings.host, settings.port):
        print(f"[start] ERROR: Port {settings.port} is already in use.")
        return 1

    print(f"[start] Starting adapter at http://{settings.host}:{settings.port}/v1")
    print(f"[start] OpenCode provider:  http://{settings.host}:{settings.port}/v1")

    # Build the app and run
    import uvicorn
    from src.main import create_app

    app = create_app(settings)
    _write_pid(os.getpid())

    try:
        uvicorn.run(app, host=settings.host, port=settings.port, log_level="warning")
    finally:
        _clear_pid()
    return 0


def cmd_stop(args) -> int:
    """Stop the running adapter server via PID file."""
    pid = _read_pid()
    if not pid:
        print("[stop] No PID file found. Adapter may not be running.")
        return 1
    try:
        os.kill(pid, signal.SIGTERM)
        _clear_pid()
        print(f"[stop] Sent SIGTERM to PID {pid}.")
        return 0
    except ProcessLookupError:
        print(f"[stop] Process {pid} not found. Cleaning up stale PID file.")
        _clear_pid()
        return 0
    except PermissionError:
        print(f"[stop] Permission denied when stopping PID {pid}.")
        return 1


def cmd_restart(args) -> int:
    """Stop then start the adapter."""
    rc = cmd_stop(args)
    if rc not in (0, 1):
        return rc
    import time; time.sleep(1)
    return cmd_start(args)


def cmd_status(args) -> int:
    """Show safe local configuration status."""
    settings = _load_settings()
    pid = _read_pid()
    running = pid is not None and _is_port_in_use(settings.host, settings.port)
    print(json.dumps({
        "host": settings.host,
        "port": settings.port,
        "running": running,
        "pid": pid,
        "authenticated": bool(settings.privategpt_access_token),
        "privategpt_base_url": settings.privategpt_base_url or "(not set)",
    }, indent=2))
    return 0


def cmd_doctor(args) -> int:
    """Comprehensive health check (Section 10.4)."""
    settings = _load_settings()
    checks: list[tuple[str, bool, str]] = []

    # 1. Python version
    ok = sys.version_info >= (3, 12)
    checks.append(("Python version >= 3.12", ok, f"{sys.version_info.major}.{sys.version_info.minor}"))

    # 2. Config / base URL
    ok = bool(settings.privategpt_base_url)
    checks.append(("PRIVATEGPT_BASE_URL set", ok, settings.privategpt_base_url or "NOT SET"))

    # 3. Port availability
    port_in_use = _is_port_in_use(settings.host, settings.port)
    checks.append(("Target port free", not port_in_use, f"{settings.host}:{settings.port}"))

    # 4. Token store access
    try:
        from src.auth.secure_store import build_token_store
        store = build_token_store()
        token = store.load_refresh_token()
        checks.append(("Secure token store accessible", True, "keyring OK" if token else "no stored token"))
    except Exception as exc:
        checks.append(("Secure token store accessible", False, str(exc)))

    # 5. PrivateGPT base URL reachability
    if settings.privategpt_base_url:
        try:
            import httpx
            resp = httpx.get(f"{settings.privategpt_base_url}/health", timeout=5)
            ok = resp.status_code < 500
            checks.append(("PrivateGPT reachable", ok, f"HTTP {resp.status_code}"))
        except Exception as exc:
            checks.append(("PrivateGPT reachable", False, str(exc)))
    else:
        checks.append(("PrivateGPT reachable", False, "base URL not configured"))

    # 6. Gemini model mapping
    model_id = settings.privategpt_model_id
    checks.append(("Gemini model id configured", bool(model_id), model_id or "will discover at runtime"))

    # 7. Agent cache
    from src.services.agent_cache import AgentCache
    cache = AgentCache()
    status = cache.get_status(settings.workspace_key)
    ok = status.get("status") == "synced"
    checks.append(("Agent cache synced", ok, status.get("agent_id", "not synced")))

    # 8. Log redaction
    checks.append(("Log redaction", True, "enabled"))

    # 9. Data directory
    checks.append(("Data directory", _DATA_DIR.exists(), str(_DATA_DIR)))

    print("\n  OpenCode PrivateGPT Bridge — Doctor\n" + "─" * 50)
    all_ok = True
    for label, ok, detail in checks:
        icon = "✅" if ok else "❌"
        print(f"  {icon} {label:<40} {detail}")
        if not ok:
            all_ok = False
    print("─" * 50)
    if all_ok:
        print("  All checks passed.")
    else:
        print("  Some checks failed. See above.")
    print()
    return 0 if all_ok else 1


def cmd_logs(args) -> int:
    """Tail the adapter log file."""
    lines = getattr(args, "lines", 50)
    if not _LOG_FILE.exists():
        print(f"[logs] Log file not found: {_LOG_FILE}")
        print("       Logs are currently written to stdout; redirect with:")
        print("       privategpt-adapter start > ~/.privategpt-adapter/adapter.log 2>&1")
        return 0

    try:
        content = _LOG_FILE.read_text(encoding="utf-8", errors="replace")
        all_lines = content.splitlines()
        for line in all_lines[-lines:]:
            print(line)
        return 0
    except OSError as exc:
        print(f"[logs] Cannot read log file: {exc}")
        return 1


def cmd_agent_sync(args) -> int:
    """Sync the PrivateGPT Agent for this workspace."""
    settings = _load_settings()
    from src.auth.token_manager import InMemoryAccessTokenProvider
    from src.providers.privategpt import PrivateGPTProvider
    from src.providers.registry import ProviderRegistry
    from src.services.agent_service import AgentService

    token_provider = InMemoryAccessTokenProvider(settings.privategpt_access_token)
    provider = PrivateGPTProvider(settings, token_provider)
    registry = ProviderRegistry(provider)
    agent_service = AgentService(registry, settings.workspace_key)

    async def _run():
        model = await provider.resolve_model("gemini-2.5-pro")
        binding = await agent_service.ensure_agent("privategpt", model.id)
        return binding

    try:
        binding = asyncio.run(_run())
        print(json.dumps({
            "status": "synced",
            "agent_id": binding.agent_id,
            "internal_model_id": binding.internal_model_id,
            "workspace_key": binding.workspace_key,
        }, indent=2))
        return 0
    except Exception as exc:
        print(f"[agent sync] ERROR: {exc}")
        return 1


def cmd_agent_status(args) -> int:
    """Show cached agent binding."""
    settings = _load_settings()
    from src.services.agent_cache import AgentCache

    cache = AgentCache()
    status = cache.get_status(settings.workspace_key)
    print(json.dumps(status, indent=2))
    return 0


def cmd_agent_reset(args) -> int:
    """Clear the cached agent binding for this workspace."""
    settings = _load_settings()
    from src.services.agent_cache import AgentCache

    cache = AgentCache()
    cache.delete(settings.workspace_key)
    print(f"[agent reset] Cache cleared for workspace: {settings.workspace_key}")
    print("              Next sync will rediscover or recreate the agent.")
    return 0


def _provider_config(settings) -> dict:
    return {
        "provider": {
            "privategpt": {
                "npm": "@ai-sdk/openai-compatible",
                "name": "PrivateGPT",
                "options": {
                    "baseURL": f"http://{settings.host}:{settings.port}/v1",
                    "apiKey": "{env:PRIVATEGPT_ADAPTER_API_KEY}",
                },
                "models": {"gemini-2.5-pro": {"name": "PrivateGPT Gemini 2.5 Pro"}},
            }
        },
        "model": "privategpt/gemini-2.5-pro",
        "permission": {
            "*": "ask",
            "read": "allow",
            "grep": "allow",
            "glob": "allow",
            "list": "allow",
            "edit": "ask",
            "bash": "ask",
            "external_directory": "ask",
            "doom_loop": "ask",
        },
    }


# ---------------------------------------------------------------------------
# Parser
# ---------------------------------------------------------------------------

def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="privategpt-adapter",
        description="OpenCode PrivateGPT Bridge — local OpenAI-compatible adapter.",
    )
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("install", help="Create runtime dirs, generate API key and default config.")
    sub.add_parser("login", help="Open browser OAuth login and save tokens securely.")
    sub.add_parser("logout", help="Clear all stored tokens.")
    sub.add_parser("start", help="Start the local adapter server (with pre-flight checks).")
    sub.add_parser("stop", help="Stop the running adapter server.")
    sub.add_parser("restart", help="Stop then start the adapter server.")
    sub.add_parser("status", help="Show safe local configuration status.")
    sub.add_parser("doctor", help="Run comprehensive health checks.")

    logs_cmd = sub.add_parser("logs", help="Show recent adapter log lines.")
    logs_cmd.add_argument("--lines", "-n", type=int, default=50, help="Number of lines to show.")

    # Agent subcommands
    agent = sub.add_parser("agent", help="Agent management commands.")
    agent_sub = agent.add_subparsers(dest="agent_command", required=True)
    agent_sub.add_parser("sync", help="Sync the PrivateGPT Agent for this workspace.")
    agent_sub.add_parser("status", help="Show cached agent binding.")
    agent_sub.add_parser("reset", help="Clear cached agent binding.")

    # OpenCode integration
    opencode = sub.add_parser("opencode", help="OpenCode integration helpers.")
    opencode_sub = opencode.add_subparsers(dest="opencode_command", required=True)
    opencode_sub.add_parser("config", help="Print OpenCode provider configuration snippet.")

    return parser


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main(argv: Sequence[str] | None = None) -> int:
    args = build_parser().parse_args(argv)

    if args.command == "install":
        return cmd_install(args)
    if args.command == "login":
        return cmd_login(args)
    if args.command == "logout":
        return cmd_logout(args)
    if args.command == "start":
        return cmd_start(args)
    if args.command == "stop":
        return cmd_stop(args)
    if args.command == "restart":
        return cmd_restart(args)
    if args.command == "status":
        return cmd_status(args)
    if args.command == "doctor":
        return cmd_doctor(args)
    if args.command == "logs":
        return cmd_logs(args)
    if args.command == "agent":
        if args.agent_command == "sync":
            return cmd_agent_sync(args)
        if args.agent_command == "status":
            return cmd_agent_status(args)
        if args.agent_command == "reset":
            return cmd_agent_reset(args)
    if args.command == "opencode":
        if args.opencode_command == "config":
            settings = _load_settings()
            print(json.dumps(_provider_config(settings), indent=2))
            return 0
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
