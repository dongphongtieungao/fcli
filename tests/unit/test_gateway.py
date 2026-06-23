"""Focused unit coverage for the confirmed OpenAI-to-Provider boundary."""

import asyncio

import pytest

from src.errors import BridgeError
from src.providers.spi import AgentSyncRequest
from src.schemas.openai import OpenAIChatRequest, OpenAIMessage
from src.schemas.unified import AgentBinding, UnifiedChatResponse
from src.services.agent_cache import AgentCache
from src.services.agent_service import AgentService
from src.services.gateway import GatewayPipeline


class NoOpAgentCache(AgentCache):
    """In-memory stub that never touches the filesystem."""

    def __init__(self):
        self._store: dict = {}

    def load(self, workspace_key: str):
        return self._store.get(workspace_key)

    def save(self, binding) -> None:
        self._store[binding.workspace_key] = binding

    def delete(self, workspace_key: str) -> None:
        self._store.pop(workspace_key, None)

    def get_status(self, workspace_key: str) -> dict:
        b = self._store.get(workspace_key)
        if b is None:
            return {"workspace_key": workspace_key, "status": "not_synced"}
        return {"workspace_key": workspace_key, "status": "synced", "agent_id": b.agent_id}


class FakeProvider:
    def __init__(self):
        self.request = None
        self.agent_request = None

    async def resolve_model(self, public_model):
        if public_model != "gemini-2.5-pro":
            raise BridgeError("model_not_supported", "unsupported")
        return type("Model", (), {"id": "private-model-id"})()

    async def ensure_agent(self, request: AgentSyncRequest):
        self.agent_request = request
        return AgentBinding(request.workspace_key, "agent-1", request.internal_model_id, request.instructions_hash)

    async def complete(self, request, agent):
        self.request = request
        return UnifiedChatResponse(content="ok")


class FakeRegistry:
    def __init__(self, provider):
        self.provider = provider

    def get_provider(self, name):
        assert name == "privategpt"
        return self.provider


def test_gateway_preserves_system_and_developer_messages():
    async def scenario():
        provider = FakeProvider()
        registry = FakeRegistry(provider)
        gateway = GatewayPipeline(registry, AgentService(registry, "workspace", agent_cache=NoOpAgentCache()))
        response = await gateway.execute_chat(
            OpenAIChatRequest(
                model="gemini-2.5-pro",
                messages=[
                    OpenAIMessage(role="system", content="system context"),
                    OpenAIMessage(role="developer", content="developer context"),
                    OpenAIMessage(role="user", content="user task"),
                ],
            )
        )
        assert response.choices[0].message.content == "ok"
        assert [message.role for message in provider.request.messages] == ["system", "developer", "user"]
        assert "Do not emit IntelliJ plugin style <tool_call> tags." in provider.agent_request.instructions

    asyncio.run(scenario())


def test_gateway_rejects_tool_payload_before_provider_call():
    async def scenario():
        provider = FakeProvider()
        registry = FakeRegistry(provider)
        gateway = GatewayPipeline(registry, AgentService(registry, "workspace", agent_cache=NoOpAgentCache()))
        with pytest.raises(BridgeError, match="Tool calling") as error:
            await gateway.execute_chat(
                OpenAIChatRequest(
                    model="gemini-2.5-pro",
                    messages=[OpenAIMessage(role="user", content="task")],
                    tools=[{"type": "function"}],
                )
            )
        assert error.value.code == "unsupported_tool_requirement"
        assert provider.request is None

    asyncio.run(scenario())
