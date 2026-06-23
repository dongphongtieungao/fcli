"""Core application error definitions."""

from typing import Any

class BridgeError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        status_code: int = 400,
        category: str = "general",
        details: dict[str, Any] | None = None,
    ):
        super().__init__(message)
        self.code = code
        self.message = message
        self.status_code = status_code
        self.category = category
        self.details = details or {}

    def as_dict(self) -> dict[str, Any]:
        return {
            "error": {
                "message": self.message,
                "type": self.category,
                "code": self.code,
                "details": self.details,
            }
        }
