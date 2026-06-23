"""Tests for the PrivateGPT endpoints evidenced by the reference plugin."""

import asyncio

import httpx

from src.auth.token_manager import InMemoryAccessTokenProvider
from src.config.settings import BridgeSettings
from src.providers.privategpt import PrivateGPTProvider
from src.providers.spi import AgentSyncRequest
from src.schemas.unified import AgentBinding, UnifiedChatRequest, UnifiedMessage


def test_provider_uses_privategpt_model_agent_and_conversation_boundaries():
    async def scenario():
        seen = []

        async def handler(request: httpx.Request) -> httpx.Response:
            seen.append((request.method, request.url.path, request.content))
            if request.url.path.endswith("support-models"):
                # Java SupportedModel uses model_id and display_name (not id/name)
                return httpx.Response(200, json=[{"model_id": "private-g25", "display_name": "Gemini 2.5 Pro"}])
            if request.url.path.endswith("discovery/created_by"):
                return httpx.Response(200, json={"items": []})
            if request.url.path.endswith("/agents"):
                return httpx.Response(200, json={"id": "agent-1"})
            if request.url.path.endswith("/conversations"):
                return httpx.Response(200, text='event: data\ndata: {"v":"hello","m":"answer"}\n\nevent: DONE\n\n')
            return httpx.Response(404)

        provider = PrivateGPTProvider(
            BridgeSettings(privategpt_base_url="https://privategpt.invalid"),
            InMemoryAccessTokenProvider("synthetic-token"),
        )
        await provider.client.client.aclose()
        provider.client.client = httpx.AsyncClient(
            base_url="https://privategpt.invalid", transport=httpx.MockTransport(handler)
        )
        model = await provider.resolve_model("gemini-2.5-pro")
        binding = await provider.ensure_agent(
            AgentSyncRequest("workspace", "hash", model.id, "PrivateGPT-Adapter-OpenCode-123", "instruction")
        )
        events = [
            event
            async for event in provider.stream(
                UnifiedChatRequest("gemini-2.5-pro", [UnifiedMessage("system", "context"), UnifiedMessage("user", "hello")], True),
                AgentBinding("workspace", binding.agent_id, model.id, "hash"),
            )
        ]
        await provider.close()

        assert [entry[:2] for entry in seen] == [
            ("GET", "/api/chat/v1/conversations/support-models"),
            ("GET", "/api/chat/v1/agents/discovery/created_by"),
            ("POST", "/api/chat/v1/agents"),
            ("POST", "/api/chat/v1/conversations"),
        ]
        assert events[0].text == "hello"
        assert events[-1].type == "completed"
        conversation_payload = seen[-1][2].decode()
        assert '"agent_id":"agent-1"' in conversation_payload
        assert '"tools":[]' in conversation_payload
        assert '"conversation_id":null' in conversation_payload

    asyncio.run(scenario())
