"""Async HTTP client wrapper."""

import httpx
from typing import Any

from src.errors import BridgeError


class HttpClient:
    def __init__(self, base_url: str, timeout: float = 180.0):
        self.base_url = base_url
        self.timeout = timeout
        self.client = httpx.AsyncClient(base_url=base_url, timeout=timeout)

    async def close(self):
        await self.client.aclose()

    async def request(self, method: str, path: str, **kwargs: Any) -> httpx.Response:
        try:
            response = await self.client.request(method, path, **kwargs)
            response.raise_for_status()
            return response
        except httpx.HTTPStatusError as exc:
            status = exc.response.status_code
            if status == 401:
                raise BridgeError(
                    "login_required",
                    "PrivateGPT authentication is required.",
                    status_code=401,
                    category="auth",
                ) from exc
            raise BridgeError(
                "upstream_http_error",
                f"Upstream returned status {status}",
                status_code=502,
                category="upstream",
                details={"status_code": status}
            ) from exc
        except httpx.RequestError as exc:
            raise BridgeError(
                "upstream_connection_error",
                "Failed to connect to upstream service.",
                status_code=502,
                category="upstream",
                details={}
            ) from exc
