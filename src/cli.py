"""Small, safe command-line surface for the local Bridge."""

import argparse
import json
from collections.abc import Sequence

from src.config.settings import BridgeSettings


def _provider_config(settings: BridgeSettings) -> dict[str, object]:
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
    }


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="privategpt-adapter")
    subparsers = parser.add_subparsers(dest="command", required=True)
    subparsers.add_parser("start", help="Run the local loopback API server.")
    subparsers.add_parser("status", help="Show safe local configuration status.")
    opencode = subparsers.add_parser("opencode", help="OpenCode integration helpers.")
    opencode.add_subparsers(dest="opencode_command", required=True).add_parser("config", help="Print provider configuration.")
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    settings = BridgeSettings.from_env()
    if args.command == "opencode":
        print(json.dumps(_provider_config(settings), indent=2))
        return 0
    if args.command == "status":
        print(json.dumps({"host": settings.host, "port": settings.port, "authenticated": bool(settings.privategpt_access_token)}))
        return 0
    if args.command == "start":
        import uvicorn

        uvicorn.run("src.main:app", host=settings.host, port=settings.port, reload=False)
        return 0
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
