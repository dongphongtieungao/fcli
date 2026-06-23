"""Environment-backed runtime configuration with safe local defaults."""

import os
import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from src.errors import BridgeError


@dataclass(frozen=True, slots=True)
class BridgeSettings:
    host: str = "127.0.0.1"
    port: int = 4100
    api_key: str | None = None
    privategpt_base_url: str | None = None
    privategpt_access_token: str | None = None
    privategpt_model_id: str | None = None
    request_timeout_seconds: float = 180.0
    max_response_chars: int = 10_000_000
    workspace_key: str = "default"

    @classmethod
    def from_env(cls) -> "BridgeSettings":
        config = _load_json_config()
        server = _section(config, "server")
        privategpt = _section(config, "privategpt")
        model = _section(config, "model")
        agent = _section(config, "agent")
        streaming = _section(config, "streaming")
        settings = cls(
            host=os.getenv("PRIVATEGPT_ADAPTER_HOST", str(server.get("host", "127.0.0.1"))),
            port=_int_env("PRIVATEGPT_ADAPTER_PORT", _int_value(server.get("port"), 4100)),
            api_key=os.getenv("PRIVATEGPT_ADAPTER_API_KEY"),
            privategpt_base_url=os.getenv("PRIVATEGPT_BASE_URL", _optional_string(privategpt.get("base_url"))),
            privategpt_access_token=os.getenv("PRIVATEGPT_ACCESS_TOKEN"),
            privategpt_model_id=os.getenv("PRIVATEGPT_MODEL_ID", _optional_string(model.get("privategpt_model_id"))),
            request_timeout_seconds=_float_env("PRIVATEGPT_REQUEST_TIMEOUT_SECONDS", _float_value(privategpt.get("request_timeout_seconds"), 180.0)),
            max_response_chars=_int_env("PRIVATEGPT_MAX_RESPONSE_CHARS", _int_value(streaming.get("max_response_chars"), 10_000_000)),
            workspace_key=os.getenv("PRIVATEGPT_ADAPTER_WORKSPACE_KEY", str(agent.get("workspace_key", "default"))),
        )
        settings.validate()
        return settings

    def validate(self) -> None:
        if self.host == "0.0.0.0":
            raise BridgeError(
                "unsafe_bind_address",
                "Binding to 0.0.0.0 requires an explicit security decision.",
                status_code=500,
                category="configuration",
            )
        if not 1 <= self.port <= 65_535:
            raise BridgeError("invalid_port", "Port must be between 1 and 65535.", category="configuration")
        if self.request_timeout_seconds <= 0:
            raise BridgeError("invalid_timeout", "Request timeout must be positive.", category="configuration")
        if self.max_response_chars <= 0:
            raise BridgeError("invalid_response_bound", "Response bound must be positive.", category="configuration")


def _int_env(name: str, default: int) -> int:
    try:
        return int(os.getenv(name, str(default)))
    except ValueError as exc:
        raise BridgeError("invalid_configuration", f"{name} must be an integer.", category="configuration") from exc


def _float_env(name: str, default: float) -> float:
    try:
        return float(os.getenv(name, str(default)))
    except ValueError as exc:
        raise BridgeError("invalid_configuration", f"{name} must be numeric.", category="configuration") from exc


def _load_json_config() -> dict[str, Any]:
    path = Path(os.getenv("PRIVATEGPT_ADAPTER_CONFIG", "config.env"))
    if not path.exists():
        return {}
    try:
        loaded = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise BridgeError("invalid_configuration", f"{path} must contain valid JSON.", category="configuration") from exc
    if not isinstance(loaded, dict):
        raise BridgeError("invalid_configuration", f"{path} must contain a JSON object.", category="configuration")
    return loaded


def _section(config: dict[str, Any], name: str) -> dict[str, Any]:
    value = config.get(name, {})
    if not isinstance(value, dict):
        raise BridgeError("invalid_configuration", f"config.env section '{name}' must be an object.", category="configuration")
    return value


def _optional_string(value: Any) -> str | None:
    return str(value) if value not in (None, "") else None


def _int_value(value: Any, default: int) -> int:
    try:
        return int(default if value is None else value)
    except (TypeError, ValueError) as exc:
        raise BridgeError("invalid_configuration", "config.env integer value is invalid.", category="configuration") from exc


def _float_value(value: Any, default: float) -> float:
    try:
        return float(default if value is None else value)
    except (TypeError, ValueError) as exc:
        raise BridgeError("invalid_configuration", "config.env numeric value is invalid.", category="configuration") from exc
