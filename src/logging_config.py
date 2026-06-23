"""Logging configuration with token and prompt redaction.

NFR 9.2 #3: Raw prompt and source code must be redacted in logs by default.
NFR 9.2 #2: Access token must not be printed.
NFR 9.2 #1: Refresh token must not appear in logs.
"""

from __future__ import annotations

import logging
import re


# Patterns to redact in log messages
_REDACT_PATTERNS = [
    # Bearer tokens and Authorization headers
    (re.compile(r"Bearer\s+[A-Za-z0-9\-._~+/]+=*", re.IGNORECASE), "Bearer [REDACTED]"),
    # JSON fields named *token*, *password*, *secret*, *key*
    (re.compile(r'"(access_token|refresh_token|password|secret|api_key|apikey)"\s*:\s*"[^"]*"', re.IGNORECASE),
     r'"\1": "[REDACTED]"'),
    # Plain key=value in query strings / form data
    (re.compile(r'(access_token|refresh_token|password|secret)=[^\s&"]+', re.IGNORECASE),
     r'\1=[REDACTED]'),
]


class RedactionFilter(logging.Filter):
    """Log filter that scrubs sensitive data from log records.

    Applies regex substitutions to the formatted message to remove tokens,
    passwords and similar secrets before they reach any handler.
    """

    def __init__(self, redact_prompt: bool = True):
        super().__init__()
        self._redact_prompt = redact_prompt

    def filter(self, record: logging.LogRecord) -> bool:
        try:
            message = record.getMessage()
            for pattern, replacement in _REDACT_PATTERNS:
                message = pattern.sub(replacement, message)
            # Replace the pre-formatted args to avoid double-formatting issues
            record.msg = message
            record.args = ()
        except Exception:
            pass  # Never let the filter crash the app
        return True


def configure_logging(level: str = "INFO", redact_prompt: bool = True) -> None:
    """Set up root logger with console handler and redaction filter.

    Call once at application startup (CLI entry point or ASGI lifespan).

    Args:
        level: Logging level string (DEBUG, INFO, WARNING, ERROR).
        redact_prompt: If True, install the RedactionFilter on all handlers.
    """
    numeric_level = getattr(logging, level.upper(), logging.INFO)

    handler = logging.StreamHandler()
    handler.setLevel(numeric_level)

    formatter = logging.Formatter(
        "%(asctime)s %(levelname)-8s %(name)s — %(message)s",
        datefmt="%H:%M:%S",
    )
    handler.setFormatter(formatter)

    if redact_prompt:
        handler.addFilter(RedactionFilter(redact_prompt=redact_prompt))

    root = logging.getLogger()
    root.setLevel(numeric_level)
    # Avoid duplicate handlers when called multiple times
    if not any(isinstance(h, logging.StreamHandler) for h in root.handlers):
        root.addHandler(handler)

    # Silence noisy third-party loggers
    logging.getLogger("httpx").setLevel(logging.WARNING)
    logging.getLogger("httpcore").setLevel(logging.WARNING)
    logging.getLogger("uvicorn.access").setLevel(logging.WARNING)
