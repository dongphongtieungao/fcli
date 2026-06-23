"""FastAPI routes for OpenCode PrivateGPT Bridge."""

from fastapi import APIRouter, Depends, Header, Request
from fastapi.responses import JSONResponse, StreamingResponse

from src.config.settings import BridgeSettings
from src.errors import BridgeError
from src.schemas.openai import OpenAIChatRequest
from src.services.gateway import GatewayPipeline


router = APIRouter()


def get_gateway(request: Request) -> GatewayPipeline:
    return request.app.state.gateway


def get_settings(request: Request) -> BridgeSettings:
    return request.app.state.settings


def verify_api_key(authorization: str | None = Header(None), settings: BridgeSettings = Depends(get_settings)):
    if not settings.api_key:
        return  # No API key configured
    if not authorization or not authorization.startswith("Bearer "):
        raise BridgeError("unauthorized", "Invalid or missing Bearer token.", status_code=401, category="auth")
    _, _, token = authorization.partition(" ")
    if token != settings.api_key:
        raise BridgeError("unauthorized", "Invalid API key.", status_code=401, category="auth")


@router.get("/health")
async def health_check():
    return {"status": "ok"}


@router.get("/v1/models")
async def list_models(_=Depends(verify_api_key)):
    return {
        "object": "list",
        "data": [
            {
                "id": "gemini-2.5-pro",
                "object": "model",
                "created": 1677610602,
                "owned_by": "privategpt"
            }
        ]
    }


@router.post("/v1/chat/completions")
async def chat_completions(
    chat_request: OpenAIChatRequest,
    gateway: GatewayPipeline = Depends(get_gateway),
    _=Depends(verify_api_key)
):
    if chat_request.stream:
        return StreamingResponse(
            gateway.execute_stream(chat_request),
            media_type="text/event-stream"
        )
    else:
        response = await gateway.execute_chat(chat_request)
        return response


def bridge_error_response(exc: BridgeError) -> JSONResponse:
    return JSONResponse(
        status_code=exc.status_code,
        content=exc.as_dict()
    )
