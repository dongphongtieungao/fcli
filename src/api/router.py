"""FastAPI routes for OpenCode PrivateGPT Bridge.

Endpoints:
  GET  /health
  GET  /auth/status
  POST /auth/login
  POST /auth/logout
  GET  /v1/models
  POST /v1/chat/completions
"""

from __future__ import annotations

import asyncio
import logging

from fastapi import APIRouter, Depends, Header, Request
from fastapi.responses import JSONResponse, StreamingResponse

from src.config.settings import BridgeSettings
from src.errors import BridgeError
from src.schemas.openai import OpenAIChatRequest
from src.services.gateway import GatewayPipeline

logger = logging.getLogger(__name__)

router = APIRouter()


# ---------------------------------------------------------------------------
# Dependency helpers
# ---------------------------------------------------------------------------

def get_gateway(request: Request) -> GatewayPipeline:
    return request.app.state.gateway


def get_settings(request: Request) -> BridgeSettings:
    return request.app.state.settings


def get_oauth_manager(request: Request):
    return getattr(request.app.state, "oauth_manager", None)


def verify_api_key(
    authorization: str | None = Header(None),
    settings: BridgeSettings = Depends(get_settings),
) -> None:
    if not settings.api_key:
        return  # No API key configured — open access (localhost-only is enforced at bind level)
    if not authorization or not authorization.startswith("Bearer "):
        raise BridgeError("unauthorized", "Invalid or missing Bearer token.", status_code=401, category="auth")
    _, _, token = authorization.partition(" ")
    if token != settings.api_key:
        raise BridgeError("unauthorized", "Invalid API key.", status_code=401, category="auth")


# ---------------------------------------------------------------------------
# Health
# ---------------------------------------------------------------------------

@router.get("/health")
async def health_check():
    return {"status": "ok"}


# ---------------------------------------------------------------------------
# Auth endpoints
# ---------------------------------------------------------------------------

@router.get("/auth/status")
async def auth_status(request: Request, _=Depends(verify_api_key)):
    """Return current login state without exposing any token value."""
    oauth_manager = get_oauth_manager(request)
    if oauth_manager is None:
        # Running without OAuth manager (env-var token mode)
        settings: BridgeSettings = request.app.state.settings
        logged_in = bool(settings.privategpt_access_token)
        return {"logged_in": logged_in, "mode": "env_var"}

    logged_in = oauth_manager.is_logged_in()
    return {
        "logged_in": logged_in,
        "mode": "oauth",
        "token_valid": oauth_manager.token_state.is_valid() if logged_in else False,
    }


@router.post("/auth/login")
async def auth_login(request: Request, _=Depends(verify_api_key)):
    """Trigger browser OAuth login flow.

    This is a convenience HTTP trigger; the primary login path is the CLI
    command ``privategpt-adapter login`` which provides interactive feedback.
    """
    oauth_manager = get_oauth_manager(request)
    if oauth_manager is None:
        raise BridgeError(
            "oauth_not_configured",
            "OAuth is not configured. Set PRIVATEGPT_BASE_URL and use 'privategpt-adapter login'.",
            status_code=501,
            category="auth",
        )
    try:
        # Run in a thread so the ASGI event loop is not blocked by the
        # synchronous OAuth callback server.
        loop = asyncio.get_event_loop()
        await loop.run_in_executor(None, oauth_manager.login)
        return {"status": "ok", "message": "Login successful."}
    except Exception as exc:
        raise BridgeError("login_failed", str(exc), status_code=500, category="auth") from exc


@router.post("/auth/logout")
async def auth_logout(request: Request, _=Depends(verify_api_key)):
    """Clear access token from memory and refresh token from secure store."""
    oauth_manager = get_oauth_manager(request)
    if oauth_manager:
        oauth_manager.logout()
    return {"status": "ok", "message": "Logged out."}


# ---------------------------------------------------------------------------
# OpenAI-compatible endpoints
# ---------------------------------------------------------------------------

@router.get("/v1/models")
async def list_models(_=Depends(verify_api_key)):
    """Return the model whitelist (FR004 — gemini-2.5-pro only)."""
    return {
        "object": "list",
        "data": [
            {
                "id": "gemini-2.5-pro",
                "object": "model",
                "created": 1677610602,
                "owned_by": "privategpt",
            }
        ],
    }


@router.post("/v1/chat/completions")
async def chat_completions(
    chat_request: OpenAIChatRequest,
    request: Request,
    gateway: GatewayPipeline = Depends(get_gateway),
    _=Depends(verify_api_key),
):
    if chat_request.stream:
        return StreamingResponse(
            _stream_with_disconnect_guard(chat_request, gateway, request),
            media_type="text/event-stream",
        )
    else:
        response = await gateway.execute_chat(chat_request)
        return response


async def _stream_with_disconnect_guard(
    chat_request: OpenAIChatRequest,
    gateway: GatewayPipeline,
    request: Request,
):
    """Relay SSE chunks while monitoring for client disconnect.

    FR009 #10: If the client disconnects, close the upstream stream and release
    the HTTP connection without starting another attempt.
    """
    async def _upstream_gen():
        async for chunk in gateway.execute_stream(chat_request):
            yield chunk

    gen = _upstream_gen()
    try:
        async for chunk in gen:
            if await request.is_disconnected():
                logger.debug("Client disconnected — aborting upstream SSE stream.")
                break
            yield chunk
    finally:
        # Ensure the async generator is closed so the upstream httpx stream
        # context manager exits and releases the connection.
        await gen.aclose()


# ---------------------------------------------------------------------------
# Error handler
# ---------------------------------------------------------------------------

def bridge_error_response(exc: BridgeError) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content=exc.as_dict())
