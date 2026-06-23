"""Secure token storage — keyring-backed with env-var fallback.

Requirement: Refresh token must not be stored as plain text (FR002, NFR 9.2 #1).
The keyring library delegates to the OS credential store:
  - Windows: Windows Credential Manager (DPAPI-encrypted)
  - macOS: Keychain
  - Linux: libsecret / kwallet

Fallback to an env-var store is provided for CI/environments where keyring
is unavailable; it is explicitly marked as insecure and emits a warning.
"""

from __future__ import annotations

import logging
import os
from abc import ABC, abstractmethod

logger = logging.getLogger(__name__)

_SERVICE_NAME = "privategpt-adapter"
_REFRESH_TOKEN_KEY = "refresh_token"


class SecureTokenStore(ABC):
    """Abstract secure storage for the refresh token."""

    @abstractmethod
    def save_refresh_token(self, token: str) -> None: ...

    @abstractmethod
    def load_refresh_token(self) -> str | None: ...

    @abstractmethod
    def delete_refresh_token(self) -> None: ...


class KeyringTokenStore(SecureTokenStore):
    """OS credential-manager backed store via the ``keyring`` library."""

    def __init__(self, service: str = _SERVICE_NAME, username: str = _REFRESH_TOKEN_KEY):
        self._service = service
        self._username = username

    def save_refresh_token(self, token: str) -> None:
        import keyring  # lazy import so absence is only a runtime issue when called
        keyring.set_password(self._service, self._username, token)
        logger.debug("Refresh token saved to keyring (%s/%s).", self._service, self._username)

    def load_refresh_token(self) -> str | None:
        import keyring
        value = keyring.get_password(self._service, self._username)
        return value or None

    def delete_refresh_token(self) -> None:
        import keyring
        try:
            keyring.delete_password(self._service, self._username)
        except Exception:
            pass  # already absent


class EnvVarTokenStore(SecureTokenStore):
    """Insecure fallback — reads from / writes to environment variable only.

    This store is *not* persistent across processes.  Tokens survive only for
    the lifetime of the current process.  Use only in CI or dev environments
    where keyring is unavailable.
    """

    _ENV_KEY = "PRIVATEGPT_REFRESH_TOKEN"

    def save_refresh_token(self, token: str) -> None:
        logger.warning(
            "EnvVarTokenStore is insecure and non-persistent. "
            "Install 'keyring' for proper secure storage."
        )
        os.environ[self._ENV_KEY] = token

    def load_refresh_token(self) -> str | None:
        return os.getenv(self._ENV_KEY) or None

    def delete_refresh_token(self) -> None:
        os.environ.pop(self._ENV_KEY, None)


def build_token_store() -> SecureTokenStore:
    """Return the best available SecureTokenStore for this environment."""
    try:
        import keyring  # noqa: F401
        store = KeyringTokenStore()
        logger.debug("Using KeyringTokenStore (OS credential manager).")
        return store
    except ImportError:
        logger.warning(
            "keyring package not installed; falling back to EnvVarTokenStore (insecure). "
            "Run: pip install keyring"
        )
        return EnvVarTokenStore()
