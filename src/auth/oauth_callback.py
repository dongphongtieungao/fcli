"""OAuth2 callback server for Microsoft Entra ID PKCE flow.

Mirrors OAuthCallbackServer.java from the Java plugin exactly:

- Listens on localhost:7070 (matching REDIRECT_URI_LOGIN = "http://localhost:7070/auth")
- /auth        → serves the fragment-capture HTML page (response_mode=fragment means
                  the code arrives in the URL hash, not the query string; the JS page
                  reads window.location.hash and redirects to /auth/code?code=...)
- /auth/code   → extracts 'code' from query string, signals the caller, stops server

Timeout: 5 minutes (matching OAuthCallbackServer.java TIMEOUT_MINUTES = 5).
"""

from __future__ import annotations

import logging
import threading
import urllib.parse
from http.server import BaseHTTPRequestHandler, HTTPServer
from typing import Callable

logger = logging.getLogger(__name__)

# Must match OAuthUrlBuilder.REDIRECT_URI_LOGIN
CALLBACK_HOST = "localhost"
CALLBACK_PORT = 7070
CALLBACK_PATH = "/auth"
TIMEOUT_MINUTES = 5

# HTML page that reads the authorization code from the URL fragment and
# redirects to /auth/code?code=... so the server can capture it via query string.
# This replicates handleFragmentCapturePage() in OAuthCallbackServer.java.
_FRAGMENT_CAPTURE_HTML = """\
<!DOCTYPE html>
<html>
<head><title>PrivateGPT Adapter — Logging in…</title></head>
<body>
<p>Completing login, please wait…</p>
<script>
  var hash = window.location.hash.substring(1);
  var params = new URLSearchParams(hash);
  var code = params.get('code');
  if (code) {
    window.location.href = '/auth/code?code=' + encodeURIComponent(code);
  } else {
    document.body.innerText = 'Login failed: no code returned from identity provider.';
  }
</script>
</body>
</html>
"""

_SUCCESS_HTML = """\
<html><body>
<h2>Login successful!</h2>
<p>You may close this tab and return to the terminal.</p>
</body></html>
"""

_FAILURE_HTML = """\
<html><body>Login failed: no authorization code in request.</body></html>
"""


class _Handler(BaseHTTPRequestHandler):
    """Handles two paths: /auth (fragment capture page) and /auth/code (code receiver)."""

    def do_GET(self) -> None:  # noqa: N802
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        if path == "/auth":
            self._serve_html(200, _FRAGMENT_CAPTURE_HTML)
            return

        if path == "/auth/code":
            params = dict(urllib.parse.parse_qsl(parsed.query))
            code = params.get("code")
            if code:
                self._serve_html(200, _SUCCESS_HTML)
                # Signal the waiting thread — runs server shutdown in background
                self.server._received_code = code  # type: ignore[attr-defined]
                threading.Thread(target=self.server.shutdown, daemon=True).start()
            else:
                self._serve_html(400, _FAILURE_HTML)
            return

        self._serve_html(404, "<html><body>Not found.</body></html>")

    def _serve_html(self, status: int, html: str) -> None:
        body = html.encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "text/html; charset=UTF-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt: str, *args: object) -> None:  # noqa: D401
        logger.debug("OAuth callback: " + fmt, *args)


def wait_for_authorization_code(
    timeout_seconds: float = TIMEOUT_MINUTES * 60,
) -> str:
    """Start callback server and block until the authorization code arrives.

    The Microsoft Entra ID authorize endpoint uses response_mode=fragment,
    meaning the code is in the URL hash. The /auth page serves a JS snippet
    that reads the hash and calls /auth/code?code=... so the server can
    extract it from the query string.

    Args:
        timeout_seconds: How long to wait before giving up.

    Returns:
        The authorization code string.

    Raises:
        TimeoutError: if no code arrives within timeout_seconds.
    """
    server = HTTPServer((CALLBACK_HOST, CALLBACK_PORT), _Handler)
    server._received_code = None  # type: ignore[attr-defined]

    # Run the server in a background thread; main thread waits on an event.
    done_event = threading.Event()

    def _serve():
        # serve_forever() blocks until shutdown() is called
        server.serve_forever(poll_interval=0.25)
        done_event.set()

    t = threading.Thread(target=_serve, daemon=True)
    t.start()
    logger.info("OAuth callback server started on %s:%d", CALLBACK_HOST, CALLBACK_PORT)

    # Wait for code or timeout
    finished = done_event.wait(timeout=timeout_seconds)

    code = server._received_code  # type: ignore[attr-defined]

    if not finished or not code:
        server.shutdown()
        raise TimeoutError(
            f"No OAuth callback received within {timeout_seconds:.0f}s. "
            f"Make sure the browser opened and you completed login."
        )

    logger.info("Authorization code received.")
    return code
