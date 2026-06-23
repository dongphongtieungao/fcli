"""Gateway Pipeline Service."""

from typing import AsyncIterator

from src.errors import BridgeError
from src.providers.registry import ProviderRegistry
from src.schemas.openai import (
    OpenAIChatRequest,
    OpenAIChatResponse,
    OpenAIChoice,
    OpenAIDelta,
    OpenAIMessage,
    OpenAIStreamChoice,
    OpenAIStreamResponse,
)
from src.schemas.unified import (
    ProviderStreamEvent,
    ProviderStreamEventType,
    UnifiedChatRequest,
    UnifiedMessage,
)
from src.services.agent_service import AgentService

import time
import uuid


class GatewayPipeline:
    def __init__(self, registry: ProviderRegistry, agent_service: AgentService):
        self.registry = registry
        self.agent_service = agent_service
        self.provider_name = "privategpt"

    async def _prepare_request(self, request: OpenAIChatRequest) -> tuple[UnifiedChatRequest, str]:
        if request.tools or request.tool_choice not in (None, "none"):
            raise BridgeError(
                "unsupported_tool_requirement",
                "Tool calling is not supported by this Bridge MVP.",
                status_code=400,
                category="validation",
            )
        provider = self.registry.get_provider(self.provider_name)
        
        # 1. Resolve model
        resolved_model = await provider.resolve_model(request.model)
        
        # 2. Ensure the stable workspace Agent. Request messages stay in the
        # conversation so system/developer context is never silently dropped.
        agent = await self.agent_service.ensure_agent(
            provider_name=self.provider_name,
            internal_model_id=resolved_model.id,
        )
        
        # 3. Build Unified Request
        unified_messages = [
            UnifiedMessage(role=m.role, content=m.content)
            for m in request.messages
        ]
        
        unified_request = UnifiedChatRequest(
            public_model=resolved_model.id,
            messages=unified_messages,
            stream=request.stream
        )
        return unified_request, agent

    async def execute_chat(self, request: OpenAIChatRequest) -> OpenAIChatResponse:
        unified_request, agent = await self._prepare_request(request)
        provider = self.registry.get_provider(self.provider_name)
        
        response = await provider.complete(unified_request, agent)
        
        return OpenAIChatResponse(
            id=f"chatcmpl-{uuid.uuid4().hex}",
            created=int(time.time()),
            model=request.model,
            choices=[
                OpenAIChoice(
                    index=0,
                    message=OpenAIMessage(role="assistant", content=response.content),
                    finish_reason=response.finish_reason or "stop"
                )
            ],
            usage=response.usage
        )

    async def execute_stream(self, request: OpenAIChatRequest) -> AsyncIterator[str]:
        unified_request, agent = await self._prepare_request(request)
        provider = self.registry.get_provider(self.provider_name)
        
        base_id = f"chatcmpl-{uuid.uuid4().hex}"
        created = int(time.time())
        
        async for event in provider.stream(unified_request, agent):
            if event.type == ProviderStreamEventType.ERROR:
                yield 'data: {"error":{"code":"upstream_error","message":"Upstream stream failed."}}\n\n'
                break
                
            if event.type == ProviderStreamEventType.COMPLETED:
                yield "data: [DONE]\n\n"
                break
                
            if event.type == ProviderStreamEventType.DELTA:
                chunk = OpenAIStreamResponse(
                    id=base_id,
                    created=created,
                    model=request.model,
                    choices=[
                        OpenAIStreamChoice(
                            index=0,
                            delta=OpenAIDelta(role="assistant", content=event.text),
                            finish_reason=event.finish_reason
                        )
                    ]
                )
                yield f"data: {chunk.model_dump_json(exclude_unset=True)}\n\n"
