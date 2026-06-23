"""Local OAuth callback server.

Spins up a temporary HTTP server on localhost to receive the OAuth redirect
after the user logs in via browser.  The server extracts the ``code``
(and optional ``state``) query parameter, then shuts down.

Usage::

    from src.auth.oauth_callback import wait_for_oauth_callback

    code = wait_for_oauth_callback(port=9876, timeout=120)
"""

from __future__ import annotations

import logging
import threading
import urllib.parse
from http.server import BaseHTTPRequestHandler, HTTPServer

logger = logging.getLogger(__name__)


class _CallbackHandler(BaseHTTPRequestHandler):
    """Minimal handler that captures the OAuth redirect and signals the event."""

    def do_GET(self) -> None:  # noqa: N802
        parsed = urllib.parse.urlparse(self.path)
        params = urllib.parse.parse_qs(parsed.query)

        code = (params.get("code") or [None])[0]
        error = (params.get("error") or [None])[0]

        self.server._oauth_code = code  # type: ignore[attr-defined]
        self.server._oauth_error = error  # type: ignore[attr-defined]

        if code:
            body = b"<html><body><h2>Login successful!</h2><p>You can close this tab and return to the terminal.</p></body></html>"
            self.send_response(200)
        else:
            body = f"<html><body><h2>Login failed</h2><p>{error or 'unknown error'}</p></body></html>".encode()
            self.send_response(400)

        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

        # Signal the waiting thread to shut the server down.
        threading.Thread(target=self.server.shutdown, daemon=True).start()

    def log_message(self, fmt: str, *args: object) -> None:  # noqa: D401
        # Route to our logger instead of stderr.
        logger.debug("OAuth callback: " + fmt, *args)


def wait_for_oauth_callback(port: int = 9876, timeout: float = 180.0) -> str:
    """Block until OAuth redirect arrives or timeout expires.

    Args:
        port: localhost port to listen on (must match the registered redirect URI).
        timeout: seconds to wait before raising ``TimeoutError``.

    Returns:
        The authorization ``code`` string.

    Raises:
        TimeoutError: if no callback is received within ``timeout`` seconds.
        RuntimeError: if the OAuth provider returned an error parameter.
    """
    server = HTTPServer(("127.0.0.1", port), _CallbackHandler)
    server._oauth_code = None  # type: ignore[attr-defined]
    server._oauth_error = None  # type: ignore[attr-defined]
    server.timeout = timeout

    logger.debug("OAuth callback server listening on 127.0.0.1:%d", port)
    server.handle_request()  # blocks until one request or timeout

    error = server._oauth_error  # type: ignore[attr-defined]
    code = server._oauth_code  # type: ignore[attr-defined]

    if error:
        raise RuntimeError(f"OAuth provider returned error: {error}")
    if not code:
        raise TimeoutError(
            f"No OAuth callback received within {timeout:.0f}s. "
            "Make sure the redirect URI is registered as http://127.0.0.1:{port}/callback."
        )
    return code
