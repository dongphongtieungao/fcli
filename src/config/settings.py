"""Environment-backed runtime configuration with safe local defaults."""

import os
from dataclasses import dataclass

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
        settings = cls(
            host=os.getenv("PRIVATEGPT_ADAPTER_HOST", "127.0.0.1"),
            port=_int_env("PRIVATEGPT_ADAPTER_PORT", 4100),
            api_key=os.getenv("PRIVATEGPT_ADAPTER_API_KEY"),
            privategpt_base_url=os.getenv("PRIVATEGPT_BASE_URL"),
            privategpt_access_token=os.getenv("PRIVATEGPT_ACCESS_TOKEN"),
            privategpt_model_id=os.getenv("PRIVATEGPT_MODEL_ID"),
            request_timeout_seconds=_float_env("PRIVATEGPT_REQUEST_TIMEOUT_SECONDS", 180.0),
            max_response_chars=_int_env("PRIVATEGPT_MAX_RESPONSE_CHARS", 10_000_000),
            workspace_key=os.getenv("PRIVATEGPT_ADAPTER_WORKSPACE_KEY", "default"),
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
