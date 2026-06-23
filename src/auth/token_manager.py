"""Auth token management — browser OAuth flow, secure storage and refresh.

FR002: Browser login
FR003: Automatic token refresh and 401-retry
NFR 9.2: No plaintext token storage; access token never logged.
"""

from __future__ import annotations

import logging
import time
import webbrowser
from dataclasses import dataclass, field
from typing import Callable

from src.auth.oauth_callback import wait_for_oauth_callback
from src.auth.secure_store import SecureTokenStore, build_token_store

logger = logging.getLogger(__name__)

# Never print the actual token values in logs.
_REDACTED = "[REDACTED]"


@dataclass
class TokenState:
    """In-memory access token holder with lightweight expiry tracking."""

    access_token: str | None = None
    expires_at: float = 0.0  # Unix timestamp; 0 means unknown/expired
    # refresh_before_expiry_seconds: if access_token expires within this window, proactively refresh.
    refresh_margin_seconds: float = 60.0

    def is_valid(self) -> bool:
        if not self.access_token:
            return False
        if self.expires_at == 0:
            return True  # unknown expiry — optimistically assume valid
        return time.time() < (self.expires_at - self.refresh_margin_seconds)

    def set(self, access_token: str, expires_in_seconds: float | None = None) -> None:
        self.access_token = access_token
        if expires_in_seconds and expires_in_seconds > 0:
            self.expires_at = time.time() + expires_in_seconds
        else:
            self.expires_at = 0

    def clear(self) -> None:
        self.access_token = None
        self.expires_at = 0


# ---------------------------------------------------------------------------
# In-memory access-token provider (kept for backwards compat with existing code)
# ---------------------------------------------------------------------------

class InMemoryAccessTokenProvider:
    """Simple in-memory wrapper around TokenState.

    Backwards-compatible with the old signature used in main.py / providers.
    """

    def __init__(self, initial_token: str | None = None):
        self._state = TokenState(access_token=initial_token)

    def get_token(self) -> str | None:
        return self._state.access_token

    def set_token(self, token: str, expires_in_seconds: float | None = None) -> None:
        self._state.set(token, expires_in_seconds)

    def clear_token(self) -> None:
        self._state.clear()

    def is_valid(self) -> bool:
        return self._state.is_valid()

    @property
    def state(self) -> TokenState:
        return self._state


# ---------------------------------------------------------------------------
# Full OAuth Token Manager
# ---------------------------------------------------------------------------

@dataclass
class OAuthTokenManager:
    """Manages the full OAuth lifecycle: login, secure storage, refresh.

    Parameters
    ----------
    base_url:
        PrivateGPT base URL (e.g. ``https://privategpt.example.internal``).
    client_id:
        OAuth client_id registered in the PrivateGPT identity provider.
    callback_port:
        Localhost port for the OAuth redirect (must match registered redirect URI).
    token_store:
        SecureTokenStore implementation; defaults to the best available store.
    token_state:
        In-memory access token holder shared with the HTTP provider.
    """

    base_url: str
    client_id: str = "privategpt-adapter"
    callback_port: int = 9876
    token_store: SecureTokenStore = field(default_factory=build_token_store)
    token_state: InMemoryAccessTokenProvider = field(default_factory=InMemoryAccessTokenProvider)

    # ------------------------------------------------------------------
    # Public interface
    # ------------------------------------------------------------------

    def login(self, callback_timeout: float = 180.0) -> None:
        """Open browser, wait for callback, exchange code, persist tokens.

        Acceptance criteria FR002:
        1. Opens browser automatically.
        2. User logs in via company IdP.
        3. Callback returns to localhost.
        4. Code is exchanged for access + refresh tokens.
        5. Refresh token saved securely; access token cached in memory.
        """
        callback_uri = f"http://127.0.0.1:{self.callback_port}/callback"
        auth_url = self._build_auth_url(callback_uri)

        print(f"Opening browser for PrivateGPT login...\n{auth_url}")
        webbrowser.open(auth_url)

        logger.info("Waiting for OAuth callback on %s ...", callback_uri)
        code = wait_for_oauth_callback(port=self.callback_port, timeout=callback_timeout)

        tokens = self._exchange_code(code, callback_uri)
        self._persist_tokens(tokens)
        logger.info("Login successful. Access token cached in memory %s.", _REDACTED)

    def logout(self) -> None:
        """Clear access token from memory and refresh token from secure store."""
        self.token_state.clear_token()
        self.token_store.delete_refresh_token()
        logger.info("Logged out — tokens cleared.")

    def restore_from_store(self) -> bool:
        """Load refresh token from secure store and exchange for access token.

        Called on startup to restore a previous session without a new browser login.

        Returns:
            True if a valid session was restored, False otherwise.
        """
        refresh_token = self.token_store.load_refresh_token()
        if not refresh_token:
            return False
        try:
            tokens = self._refresh(refresh_token)
            self._persist_tokens(tokens)
            logger.info("Session restored from secure store.")
            return True
        except Exception as exc:
            logger.warning("Could not restore session from stored token: %s", exc)
            return False

    def refresh_access_token(self) -> bool:
        """Proactively refresh the access token.

        FR003: Refresh before expiry and retry once on 401.

        Returns:
            True if refresh succeeded, False otherwise.
        """
        refresh_token = self.token_store.load_refresh_token()
        if not refresh_token:
            logger.warning("No refresh token in store — user must log in again.")
            return False
        try:
            tokens = self._refresh(refresh_token)
            self._persist_tokens(tokens)
            logger.debug("Access token refreshed %s.", _REDACTED)
            return True
        except Exception as exc:
            logger.warning("Token refresh failed: %s", exc)
            return False

    def get_valid_access_token(self) -> str | None:
        """Return a valid access token, refreshing proactively if needed.

        Returns None if the user is not logged in and refresh fails.
        """
        if not self.token_state.is_valid():
            if not self.refresh_access_token():
                return None
        return self.token_state.get_token()

    def is_logged_in(self) -> bool:
        """Return True if a usable token exists (in-memory or refreshable)."""
        if self.token_state.is_valid():
            return True
        return bool(self.token_store.load_refresh_token())

    # ------------------------------------------------------------------
    # Private helpers
    # ------------------------------------------------------------------

    def _build_auth_url(self, redirect_uri: str) -> str:
        """Construct the OAuth authorization URL.

        NOTE: The exact PrivateGPT OAuth endpoint and params are a runtime-
        validation condition (see requirement section 16).  The URL below is
        constructed from the observed plugin pattern; adjust when a redacted
        capture confirms the real endpoint.
        """
        import urllib.parse
        params = urllib.parse.urlencode({
            "response_type": "code",
            "client_id": self.client_id,
            "redirect_uri": redirect_uri,
        })
        return f"{self.base_url}/oauth/authorize?{params}"

    def _exchange_code(self, code: str, redirect_uri: str) -> dict:
        """POST authorization code to the token endpoint; return token dict."""
        import httpx
        url = f"{self.base_url}/oauth/token"
        resp = httpx.post(url, data={
            "grant_type": "authorization_code",
            "code": code,
            "redirect_uri": redirect_uri,
            "client_id": self.client_id,
        }, timeout=30)
        resp.raise_for_status()
        return resp.json()

    def _refresh(self, refresh_token: str) -> dict:
        """POST refresh_token grant; return new token dict."""
        import httpx
        url = f"{self.base_url}/oauth/token"
        resp = httpx.post(url, data={
            "grant_type": "refresh_token",
            "refresh_token": refresh_token,
            "client_id": self.client_id,
        }, timeout=30)
        resp.raise_for_status()
        return resp.json()

    def _persist_tokens(self, tokens: dict) -> None:
        """Cache access token in memory; persist refresh token to secure store."""
        access_token = tokens.get("access_token")
        refresh_token = tokens.get("refresh_token")
        expires_in = tokens.get("expires_in")

        if access_token:
            self.token_state.set_token(
                access_token,
                expires_in_seconds=float(expires_in) if expires_in else None
            )

        if refresh_token:
            self.token_store.save_refresh_token(refresh_token)

        if not access_token:
            raise RuntimeError("Token endpoint did not return an access_token.")


# ---------------------------------------------------------------------------
# Factory
# ---------------------------------------------------------------------------

def build_token_manager(base_url: str, initial_access_token: str | None = None) -> OAuthTokenManager:
    """Build an OAuthTokenManager wired to an InMemoryAccessTokenProvider."""
    provider = InMemoryAccessTokenProvider(initial_token=initial_access_token)
    manager = OAuthTokenManager(
        base_url=base_url,
        token_state=provider,
    )
    return manager
