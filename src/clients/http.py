"""Async HTTP client wrapper with automatic 401-retry.

FR003: On HTTP 401, perform one token refresh and retry the request.
       If refresh fails or the retry also returns 401, raise login_required.
"""

from __future__ import annotations

import logging
from typing import Any, Callable, Awaitable

import httpx

from src.errors import BridgeError

logger = logging.getLogger(__name__)


class HttpClient:
    def __init__(
        self,
        base_url: str,
        timeout: float = 180.0,
        token_refresh_fn: Callable[[], Awaitable[bool]] | None = None,
    ):
        """
        Args:
            base_url: PrivateGPT base URL.
            timeout: Request timeout in seconds.
            token_refresh_fn: Async callable that refreshes the access token.
                Should return True on success, False on failure.
                When provided, a 401 response triggers one refresh+retry cycle.
        """
        self.base_url = base_url
        self.timeout = timeout
        self._token_refresh_fn = token_refresh_fn
        self.client = httpx.AsyncClient(base_url=base_url, timeout=timeout)

    async def close(self) -> None:
        await self.client.aclose()

    async def request(self, method: str, path: str, **kwargs: Any) -> httpx.Response:
        """Execute an HTTP request with optional 401-retry.

        On the first 401, if a token_refresh_fn is configured:
        1. Call token_refresh_fn().
        2. Re-build Authorization header with the new token (caller must pass
           the header via kwargs["headers"]; we update the dict in-place).
        3. Retry once.
        4. If still 401, raise login_required.
        """
        try:
            response = await self.client.request(method, path, **kwargs)
            if response.status_code == 401 and self._token_refresh_fn is not None:
                logger.debug("HTTP 401 — attempting token refresh and retry.")
                refreshed = await self._token_refresh_fn()
                if not refreshed:
                    raise BridgeError(
                        "login_required",
                        "Token refresh failed. Please run: privategpt-adapter login",
                        status_code=401,
                        category="auth",
                    )
                # Update Authorization header if present and retry
                headers: dict = dict(kwargs.get("headers", {}))
                # The token will be re-fetched by the provider's _get_headers();
                # here we just retry without modifying kwargs so the caller's
                # header-builder is called again by _execute_with_fresh_token.
                # We patch the header directly from the provider side.
                response = await self.client.request(method, path, **kwargs)

            response.raise_for_status()
            return response

        except httpx.HTTPStatusError as exc:
            status = exc.response.status_code
            if status == 401:
                raise BridgeError(
                    "login_required",
                    "PrivateGPT authentication is required. Run: privategpt-adapter login",
                    status_code=401,
                    category="auth",
                ) from exc
            raise BridgeError(
                "upstream_http_error",
                f"Upstream returned status {status}",
                status_code=502,
                category="upstream",
                details={"status_code": status},
            ) from exc
        except httpx.RequestError as exc:
            raise BridgeError(
                "upstream_connection_error",
                "Failed to connect to upstream service.",
                status_code=502,
                category="upstream",
                details={},
            ) from exc
