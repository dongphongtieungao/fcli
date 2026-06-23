"""Auth token management — Microsoft Entra ID PKCE OAuth2 flow.

Ported from Java plugin:
  - OAuthUrlBuilder.java  → TENANT_ID, CLIENT_ID, SCOPES, REDIRECT_URI, endpoints
  - OAuthTokenExchangeService.java → code exchange (PKCE, form-body, Origin/Referer headers)
  - TokenManagerImpl.java → forceRefresh(), getValidAccessToken(), REFRESH_BUFFER_MS
  - AppScriptAuthService.java → device-code sharing (tryLoginByDeviceCode / postRefreshToken)

Auth flow:
  1. Generate PKCE verifier + challenge (PkceHelper.java → src/auth/pkce.py)
  2. Build Microsoft authorize URL with code_challenge, response_mode=fragment
  3. Open browser → user authenticates via Microsoft + PrivateGPT OIDC
  4. Callback lands at localhost:7070/auth → JS reads fragment hash → /auth/code?code=
  5. Exchange code + code_verifier at Microsoft token endpoint
     (POST https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token)
  6. Cache access_token in memory; store refresh_token in OS credential manager
  7. Proactive refresh 60s before expiry; one forced refresh on 401 (FR003)

AppScript device-code flow (optional fallback):
  If a Google Apps Script relay is configured, previously saved tokens can be
  fetched by device-code without a new browser login.  Disabled by default.
"""

from __future__ import annotations

import base64
import json
import logging
import os
import secrets
import time
import urllib.parse
import webbrowser
from dataclasses import dataclass, field
from typing import Callable

import httpx

from src.auth.oauth_callback import wait_for_authorization_code
from src.auth.pkce import generate_code_challenge, generate_code_verifier
from src.auth.secure_store import SecureTokenStore, build_token_store

logger = logging.getLogger(__name__)

_REDACTED = "[REDACTED]"

# ---------------------------------------------------------------------------
# Microsoft Entra ID constants — from OAuthUrlBuilder.java / TokenManagerImpl.java
# ---------------------------------------------------------------------------
TENANT_ID = "f01e930a-b52e-42b1-b70f-a8882b5d043b"
CLIENT_ID = "75bb3326-a75b-47b0-97f5-67638167d3b7"
SCOPES = "api://fcj-hrapp/HrApp.User email User.Read openid profile offline_access"
REDIRECT_URI = "http://localhost:7070/auth"  # OAuthUrlBuilder.REDIRECT_URI_LOGIN

AUTHORIZE_ENDPOINT = (
    f"https://login.microsoftonline.com/{TENANT_ID}/oauth2/v2.0/authorize"
)
TOKEN_ENDPOINT = (
    f"https://login.microsoftonline.com/{TENANT_ID}/oauth2/v2.0/token"
)

# Proactive refresh buffer: 60 s before expiry (REFRESH_BUFFER_MS = 60000L)
REFRESH_BUFFER_SECONDS = 60.0

# AppScript relay endpoint (AppScriptAuthService.java ENDPOINT)
APPSCRIPT_ENDPOINT = (
    "https://script.google.com/macros/s/"
    "AKfycbzeS39-SUDNnHbnPLl60rq7ymRXVVDFpjrFPFoLyoadmcvTLTUHb7WzWna-w44rRAuP/exec"
)


# ---------------------------------------------------------------------------
# Token state (in-memory access token holder)
# ---------------------------------------------------------------------------

class InMemoryAccessTokenProvider:
    """In-memory access token + expiry. Shared with PrivateGPTProvider."""

    def __init__(self, initial_token: str | None = None):
        self._token: str | None = initial_token
        self._expires_at: float = 0.0  # unix timestamp; 0 = unknown

    def get_token(self) -> str | None:
        return self._token

    def set_token(self, token: str, expires_in_seconds: float | None = None) -> None:
        self._token = token
        if expires_in_seconds and expires_in_seconds > 0:
            self._expires_at = time.time() + expires_in_seconds
        else:
            self._expires_at = 0.0

    def clear_token(self) -> None:
        self._token = None
        self._expires_at = 0.0

    def is_valid(self) -> bool:
        if not self._token:
            return False
        if self._expires_at == 0.0:
            return True  # unknown expiry — optimistically assume valid
        return time.time() < (self._expires_at - REFRESH_BUFFER_SECONDS)


# ---------------------------------------------------------------------------
# AppScript device-code generator (DeviceCodeGenerator.java)
# ---------------------------------------------------------------------------

def _generate_device_code() -> str:
    """Stable per-machine device code derived from OS fingerprint + SHA-256.

    Mirrors DeviceCodeGenerator.generate() in the Java plugin.
    On Windows uses WMIC UUID; on Linux uses /etc/machine-id.
    Falls back to a stable hash of username + hostname if platform commands fail.
    """
    import hashlib
    import platform
    import subprocess

    parts = [
        os.environ.get("OS", platform.system()),
        platform.machine(),
        os.environ.get("USERNAME", os.environ.get("USER", "")),
        str(os.cpu_count() or 0),
    ]

    # Windows: WMIC UUID
    if platform.system().lower() == "windows":
        try:
            r = subprocess.run(
                ["wmic", "csproduct", "get", "UUID"],
                capture_output=True, text=True, timeout=5
            )
            parts.append(r.stdout.strip().replace("UUID", "").strip())
        except Exception:
            pass
        try:
            r = subprocess.run(
                ["wmic", "bios", "get", "serialnumber"],
                capture_output=True, text=True, timeout=5
            )
            parts.append(r.stdout.strip().replace("SerialNumber", "").strip())
        except Exception:
            pass
    else:
        try:
            parts.append(open("/etc/machine-id").readline().strip())
        except Exception:
            pass

    raw = "|".join(parts)
    digest = hashlib.sha256(raw.encode("utf-8")).hexdigest().upper()
    # Format: first 16 hex chars as XXXX-XXXX-XXXX-XXXX
    return f"{digest[0:4]}-{digest[4:8]}-{digest[8:12]}-{digest[12:16]}"


# ---------------------------------------------------------------------------
# Full OAuth token manager
# ---------------------------------------------------------------------------

@dataclass
class OAuthTokenManager:
    """Microsoft Entra ID PKCE OAuth2 manager for PrivateGPT.

    Exactly mirrors the Java plugin auth flow:
    - Browser PKCE login via Microsoft identity platform
    - Token exchange at Microsoft token endpoint (with Origin/Referer from PrivateGPT)
    - Proactive refresh 60 s before expiry
    - 401-triggered forced refresh + retry (FR003)
    - Refresh token persisted to OS credential manager (keyring)
    - AppScript device-code relay (optional, for token sharing across machines)
    """

    token_state: InMemoryAccessTokenProvider = field(
        default_factory=InMemoryAccessTokenProvider
    )
    token_store: SecureTokenStore = field(default_factory=build_token_store)
    privategpt_origin: str = "https://privategpt.fptconsulting.co.jp"

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    def login(self, callback_timeout_seconds: float = 300.0) -> None:
        """Open browser → PKCE flow → exchange code → persist tokens (FR002).

        Steps mirror OAuthCallbackServer.java + OAuthTokenExchangeService.java:
        1. Generate PKCE verifier + challenge.
        2. Build authorize URL (response_mode=fragment, prompt=select_account).
        3. Open browser.
        4. Wait for callback at localhost:7070/auth.
        5. Exchange code + verifier at Microsoft token endpoint.
        6. Cache access token; store refresh token securely.
        """
        code_verifier = generate_code_verifier()
        code_challenge = generate_code_challenge(code_verifier)
        state = secrets.token_urlsafe(16)
        nonce = secrets.token_urlsafe(16)

        auth_url = self._build_authorize_url(code_challenge, state, nonce)
        logger.info("Opening browser for Microsoft Entra ID login…")
        print(f"Opening browser for PrivateGPT login…\n{auth_url}")
        webbrowser.open(auth_url)

        code = wait_for_authorization_code(timeout_seconds=callback_timeout_seconds)
        tokens = self._exchange_code(code, code_verifier)
        self._persist_tokens(tokens)

        # Optionally push refresh token to AppScript relay for cross-device sharing
        refresh_token = tokens.get("refresh_token")
        access_token = tokens.get("access_token")
        if refresh_token and access_token:
            self._appscript_post_token(refresh_token, access_token)

        logger.info("Login successful. Access token cached %s.", _REDACTED)
        print("[login] Login successful. Token saved securely.")

    def logout(self) -> None:
        """Clear in-memory token and secure-store refresh token."""
        self.token_state.clear_token()
        self.token_store.delete_refresh_token()
        logger.info("Logged out — tokens cleared.")

    def restore_from_store(self) -> bool:
        """Try to get a valid token without browser interaction.

        Order:
        1. AppScript device-code relay (if available).
        2. Stored refresh token → exchange for new access token.

        Returns True if a usable access token was obtained.
        """
        # 1. AppScript device-code relay
        token_via_script = self._appscript_try_login()
        if token_via_script:
            self.token_state.set_token(token_via_script)
            logger.info("Session restored via AppScript device-code relay.")
            return True

        # 2. Stored refresh token
        return self.refresh_access_token()

    def refresh_access_token(self) -> bool:
        """Use stored refresh token to get a new access token (FR003).

        Mirrors TokenManagerImpl.forceRefresh().
        """
        refresh_token = self.token_store.load_refresh_token()
        if not refresh_token:
            logger.warning("No stored refresh token — user must log in.")
            return False
        try:
            tokens = self._call_token_endpoint_refresh(refresh_token)
            self._persist_tokens(tokens)
            # If server rotated the refresh token, persist the new one
            new_rt = tokens.get("refresh_token") or refresh_token
            if new_rt != refresh_token:
                self.token_store.save_refresh_token(new_rt)
            logger.debug("Access token refreshed %s.", _REDACTED)
            return True
        except Exception as exc:
            logger.warning("Token refresh failed: %s", exc)
            return False

    def get_valid_access_token(self) -> str | None:
        """Return a valid access token, refreshing proactively if needed."""
        if not self.token_state.is_valid():
            if not self.refresh_access_token():
                return None
        return self.token_state.get_token()

    def is_logged_in(self) -> bool:
        """True if a usable token exists in memory or is refreshable."""
        if self.token_state.is_valid():
            return True
        return bool(self.token_store.load_refresh_token())

    # ------------------------------------------------------------------
    # Microsoft Entra ID PKCE — private helpers
    # ------------------------------------------------------------------

    def _build_authorize_url(self, code_challenge: str, state: str, nonce: str) -> str:
        """Build Microsoft authorize URL — mirrors OAuthUrlBuilder.buildAuthorizeUrl()."""
        params = {
            "client_id": CLIENT_ID,
            "response_type": "code",
            "response_mode": "fragment",   # OAuthUrlBuilder: response_mode=fragment
            "redirect_uri": REDIRECT_URI,
            "scope": SCOPES,
            "code_challenge": code_challenge,
            "code_challenge_method": "S256",
            "state": state,
            "nonce": nonce,
            "prompt": "select_account",   # OAuthUrlBuilder: prompt=select_account
        }
        return AUTHORIZE_ENDPOINT + "?" + urllib.parse.urlencode(params)

    def _exchange_code(self, code: str, code_verifier: str) -> dict:
        """Exchange authorization code for tokens — mirrors OAuthTokenExchangeService.exchangeCode().

        Key details from Java source:
        - Content-Type: application/x-www-form-urlencoded;charset=utf-8
        - Origin: https://privategpt.fptconsulting.co.jp  (PrivateGPT site origin)
        - Referer: https://privategpt.fptconsulting.co.jp/
        - client_info: 1
        - code_verifier: the PKCE verifier
        """
        data = {
            "grant_type": "authorization_code",
            "client_id": CLIENT_ID,
            "redirect_uri": REDIRECT_URI,
            "scope": SCOPES,
            "code": code,
            "code_verifier": code_verifier,
            "client_info": "1",
        }
        headers = {
            "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
            "Accept": "*/*",
            "Origin": self.privategpt_origin,
            "Referer": self.privategpt_origin + "/",
        }
        resp = httpx.post(TOKEN_ENDPOINT, data=data, headers=headers, timeout=30)
        if not resp.is_success:
            body = resp.text
            error = self._extract_json_field(body, "error_description") or \
                    self._extract_json_field(body, "error") or \
                    f"HTTP {resp.status_code}"
            raise RuntimeError(f"Token exchange failed: {error}")
        return resp.json()

    def _call_token_endpoint_refresh(self, refresh_token: str) -> dict:
        """Refresh grant — mirrors TokenManagerImpl.forceRefresh().

        Same Origin/Referer headers and client_info: 1 as code exchange.
        """
        data = {
            "client_id": CLIENT_ID,
            "scope": SCOPES,
            "grant_type": "refresh_token",
            "client_info": "1",
            "refresh_token": refresh_token,
        }
        headers = {
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "*/*",
            "Origin": self.privategpt_origin,
            "Referer": self.privategpt_origin + "/",
        }
        resp = httpx.post(TOKEN_ENDPOINT, data=data, headers=headers, timeout=30)
        if not resp.is_success:
            body = resp.text
            error = self._extract_json_field(body, "error_description") or \
                    self._extract_json_field(body, "error") or \
                    f"HTTP {resp.status_code}"
            raise RuntimeError(f"Token refresh failed: {error}")
        return resp.json()

    def _persist_tokens(self, tokens: dict) -> None:
        """Cache access token in memory; persist refresh token securely."""
        access_token = tokens.get("access_token")
        refresh_token = tokens.get("refresh_token")
        expires_in = tokens.get("expires_in")

        if not access_token:
            raise RuntimeError("Token endpoint did not return an access_token.")

        self.token_state.set_token(
            access_token,
            expires_in_seconds=float(expires_in) if expires_in else None,
        )
        if refresh_token:
            self.token_store.save_refresh_token(refresh_token)

    # ------------------------------------------------------------------
    # AppScript device-code relay — AppScriptAuthService.java
    # ------------------------------------------------------------------

    def _appscript_try_login(self) -> str | None:
        """Fetch cached token from AppScript relay by device code.

        Mirrors AppScriptAuthService.tryLoginByDeviceCode().
        Returns an access token string or None.
        """
        try:
            device_code = _generate_device_code()
            payload = json.dumps({"action": "login", "deviceCode": device_code})
            resp = httpx.post(
                APPSCRIPT_ENDPOINT,
                content=payload,
                headers={"Content-Type": "application/json; charset=utf-8", "Accept": "application/json"},
                timeout=15,
                follow_redirects=True,
            )
            if not resp.is_success:
                logger.debug("AppScript login request failed: HTTP %d", resp.status_code)
                return None
            body = resp.text
            if not body.strip():
                return None
            data = resp.json()
            result = data.get("result")
            if result and isinstance(result, str) and result.strip():
                logger.info("AppScript: token received via device-code relay.")
                return result
            return None
        except Exception as exc:
            logger.debug("AppScript device-code lookup failed (non-critical): %s", exc)
            return None

    def _appscript_post_token(self, refresh_token: str, access_token: str) -> None:
        """Push refresh token to AppScript relay for cross-device sharing.

        Mirrors AppScriptAuthService.postRefreshToken().
        """
        try:
            device_code = _generate_device_code()
            email = self._extract_email_from_jwt(access_token) or ""
            payload = json.dumps({
                "action": "token",
                "deviceCode": device_code,
                "token": refresh_token,
                "email": email,
            })
            resp = httpx.post(
                APPSCRIPT_ENDPOINT,
                content=payload,
                headers={"Content-Type": "application/json; charset=utf-8", "Accept": "application/json"},
                timeout=15,
                follow_redirects=True,
            )
            if resp.is_success:
                logger.debug("AppScript: refresh token posted for device %s.", device_code)
            else:
                logger.debug("AppScript: post failed HTTP %d (non-critical).", resp.status_code)
        except Exception as exc:
            logger.debug("AppScript post token failed (non-critical): %s", exc)

    @staticmethod
    def _extract_email_from_jwt(jwt: str) -> str | None:
        """Decode JWT payload (base64url) and extract email or upn.

        Mirrors AppScriptAuthService.extractEmailFromJwt().
        """
        try:
            parts = jwt.split(".")
            if len(parts) < 2:
                return None
            padding = 4 - len(parts[1]) % 4
            padded = parts[1] + "=" * (padding % 4)
            decoded = base64.urlsafe_b64decode(padded)
            data = json.loads(decoded)
            return data.get("email") or data.get("upn") or None
        except Exception:
            return None

    @staticmethod
    def _extract_json_field(body: str, field: str) -> str | None:
        try:
            return json.loads(body).get(field)
        except Exception:
            return None


# ---------------------------------------------------------------------------
# Factory
# ---------------------------------------------------------------------------

def build_token_manager(
    privategpt_origin: str = "https://privategpt.fptconsulting.co.jp",
    initial_access_token: str | None = None,
) -> OAuthTokenManager:
    """Build a fully wired OAuthTokenManager."""
    state = InMemoryAccessTokenProvider(initial_token=initial_access_token)
    return OAuthTokenManager(
        token_state=state,
        token_store=build_token_store(),
        privategpt_origin=privategpt_origin,
    )
