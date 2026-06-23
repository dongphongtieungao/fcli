"""Application composition root for OpenCode PrivateGPT Bridge."""

from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import uvicorn

from src.api.router import bridge_error_response, router
from src.auth.token_manager import InMemoryAccessTokenProvider
from src.config.settings import BridgeSettings
from src.errors import BridgeError
from src.providers.privategpt import PrivateGPTProvider
from src.providers.registry import ProviderRegistry
from src.services.agent_service import AgentService
from src.services.gateway import GatewayPipeline


def create_app() -> FastAPI:
    settings = BridgeSettings.from_env()
    token_provider = InMemoryAccessTokenProvider(settings.privategpt_access_token)

    privategpt_provider = PrivateGPTProvider(settings, token_provider)
    registry = ProviderRegistry(privategpt_provider)

    agent_service = AgentService(registry, settings.workspace_key)
    gateway = GatewayPipeline(registry, agent_service)

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        # Startup
        yield
        # Shutdown
        await privategpt_provider.close()

    app = FastAPI(title="OpenCode PrivateGPT Bridge", version="0.1.0", lifespan=lifespan)

    app.state.settings = settings
    app.state.gateway = gateway

    app.include_router(router)

    @app.exception_handler(BridgeError)
    async def handle_bridge_error(request: Request, exc: BridgeError):
        return bridge_error_response(exc)

    @app.exception_handler(Exception)
    async def handle_generic_error(request: Request, exc: Exception):
        return JSONResponse(
            status_code=500,
            content={
                "error": {
                    "message": "Internal server error.",
                    "type": "internal",
                    "code": "internal_error",
                    "details": {}
                }
            }
        )

    return app


app = create_app()


if __name__ == "__main__":
    import os
    host = os.getenv("PRIVATEGPT_ADAPTER_HOST", "127.0.0.1")
    port = int(os.getenv("PRIVATEGPT_ADAPTER_PORT", "4100"))
    uvicorn.run("src.main:app", host=host, port=port, reload=False)
