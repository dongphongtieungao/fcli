"""PrivateGPT implementation of the Provider SPI.

Wires the OAuthTokenManager for automatic 401-retry (FR003).
"""

from __future__ import annotations

import json
import logging
from typing import Any, AsyncIterator, Sequence

from src.auth.token_manager import InMemoryAccessTokenProvider, OAuthTokenManager
from src.clients.http import HttpClient
from src.config.settings import BridgeSettings
from src.errors import BridgeError
from src.providers.spi import (
    AgentSyncRequest,
    ProviderAdapter,
    ProviderHealth,
    ProviderModel,
)
from src.schemas.unified import (
    AgentBinding,
    ProviderError,
    ProviderStreamEvent,
    ProviderStreamEventType,
    UnifiedChatRequest,
    UnifiedChatResponse,
)

logger = logging.getLogger(__name__)


class PrivateGPTProvider(ProviderAdapter):
    def __init__(
        self,
        settings: BridgeSettings,
        token_provider: InMemoryAccessTokenProvider,
        oauth_manager: OAuthTokenManager | None = None,
    ):
        self.settings = settings
        self.token_provider = token_provider
        self._oauth_manager = oauth_manager
        self.public_model_id = "gemini-2.5-pro"
        self.internal_model_id = settings.privategpt_model_id

        # Wire 401-retry through the token manager if available.
        refresh_fn = self._do_token_refresh if oauth_manager else None
        self.client = HttpClient(
            base_url=settings.privategpt_base_url or "http://localhost:8001",
            timeout=settings.request_timeout_seconds,
            token_refresh_fn=refresh_fn,
        )

    async def _do_token_refresh(self) -> bool:
        """Async wrapper for OAuthTokenManager.refresh_access_token (FR003)."""
        if self._oauth_manager is None:
            return False
        result = self._oauth_manager.refresh_access_token()
        # After refresh, update the in-memory provider so _get_headers() picks it up.
        new_token = self._oauth_manager.token_state.get_token()
        if new_token:
            self.token_provider.set_token(new_token)
        return result

    def _get_headers(self) -> dict[str, str]:
        token = self.token_provider.get_token()
        if not token:
            raise BridgeError(
                "login_required",
                "Authentication required. Run: privategpt-adapter login",
                status_code=401,
                category="auth",
            )
        return {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        }

    async def initialize(self) -> None:
        pass

    async def close(self) -> None:
        await self.client.close()

    async def health_check(self) -> ProviderHealth:
        try:
            await self.client.request("GET", "/health")
            return ProviderHealth(status="ok")
        except Exception:
            return ProviderHealth(status="unreachable")

    async def list_models(self) -> Sequence[ProviderModel]:
        # Public API is deliberately whitelisted even when discovery returns
        # additional PrivateGPT models.
        return [ProviderModel(id=self.public_model_id, name="PrivateGPT Gemini 2.5 Pro")]

    async def resolve_model(self, public_model: str) -> ProviderModel:
        if public_model != self.public_model_id:
            raise BridgeError(
                "model_not_supported",
                f"Model {public_model} is not supported. Only gemini-2.5-pro is available.",
                status_code=400,
                category="validation",
            )
        if self.internal_model_id:
            return ProviderModel(id=self.internal_model_id, name="PrivateGPT Gemini 2.5 Pro")
        # Discover from PrivateGPT support-models endpoint
        response = await self.client.request(
            "GET", "/api/chat/v1/conversations/support-models", headers=self._get_headers()
        )
        models = response.json()
        if not isinstance(models, list):
            raise BridgeError(
                "model_not_available",
                "PrivateGPT model discovery returned an invalid response.",
                502,
                "upstream",
            )
        for model in models:
            if not isinstance(model, dict):
                continue
            # Java SupportedModel.java uses @SerializedName("model_id") and @SerializedName("display_name")
            # There is no "id" or "name" field in the Java schema.
            identifier = model.get("model_id")
            display_name = model.get("display_name") or ""
            if identifier and (
                identifier == self.public_model_id
                or "gemini 2.5 pro" in str(display_name).lower()
                or "gemini-2.5-pro" in str(identifier).lower()
            ):
                self.internal_model_id = str(identifier)
                logger.info("Resolved Gemini 2.5 Pro internal model id: %s", self.internal_model_id)
                return ProviderModel(id=self.internal_model_id, name=display_name or "PrivateGPT Gemini 2.5 Pro")
        raise BridgeError(
            "model_not_available",
            "Gemini 2.5 Pro is not available from PrivateGPT.",
            502,
            "upstream",
        )

    async def ensure_agent(self, request: AgentSyncRequest) -> AgentBinding:
        try:
            # Fetch all pages — mirrors AgentApiClientImpl.listMyAgents() pagination.
            # Java fetches until page.getItems().size() < PAGE_SIZE (50).
            # Without pagination, agents beyond page 1 (> 50) are invisible and
            # the adapter would create a duplicate agent every startup.
            all_agents: list[dict] = []
            offset = 0
            page_size = 50
            while True:
                resp = await self.client.request(
                    "GET",
                    f"/api/chat/v1/agents/discovery/created_by?offset={offset}&limit={page_size}",
                    headers=self._get_headers(),
                )
                page_data = resp.json()
                items = page_data.get("items", []) if isinstance(page_data, dict) else page_data
                if not isinstance(items, list):
                    raise BridgeError(
                        "agent_sync_failed",
                        "PrivateGPT Agent discovery returned an invalid response.",
                        502,
                        "agent",
                    )
                all_agents.extend(items)
                if len(items) < page_size:
                    break  # Last page reached
                offset += page_size

            existing = next(
                (a for a in all_agents if isinstance(a, dict) and a.get("name") == request.name),
                None,
            )
            payload: dict[str, Any] = {
                "name": request.name,
                "description": "OpenCode coding agent for local workspace",
                "instructions": request.instructions,
                "model_id": request.internal_model_id,
                "allow_download": True,
                "show_instructions": True,
                "sample_questions": [],
                "category": None,
            }
            if existing and existing.get("id"):
                logger.debug("Updating existing agent %s.", existing["id"])
                agent_response = await self.client.request(
                    "PUT",
                    f"/api/chat/v1/agents/{existing['id']}",
                    json=payload,
                    headers=self._get_headers(),
                )
            else:
                logger.debug("Creating new PrivateGPT agent '%s'.", request.name)
                agent_response = await self.client.request(
                    "POST", "/api/chat/v1/agents", json=payload, headers=self._get_headers()
                )
            agent_data = agent_response.json()
            agent_id = agent_data.get("id") if isinstance(agent_data, dict) else None
            if not agent_id:
                raise BridgeError(
                    "agent_sync_failed",
                    "PrivateGPT Agent response did not include an ID.",
                    502,
                    "agent",
                )
            return AgentBinding(
                workspace_key=request.workspace_key,
                agent_id=agent_id,
                internal_model_id=request.internal_model_id,
                instructions_hash=request.instructions_hash,
            )
        except BridgeError:
            raise
        except Exception as exc:
            raise BridgeError(
                "agent_sync_failed",
                "Failed to synchronize agent.",
                status_code=502,
                category="agent",
            ) from exc

    async def complete(self, request: UnifiedChatRequest, agent: AgentBinding) -> UnifiedChatResponse:
        content = ""
        async for event in self.stream(request, agent):
            if event.type == ProviderStreamEventType.DELTA:
                content += event.text or ""
            elif event.type == ProviderStreamEventType.ERROR:
                raise BridgeError(
                    "upstream_error",
                    "PrivateGPT did not complete the conversation.",
                    502,
                    "upstream",
                )
        return UnifiedChatResponse(content=content, finish_reason="stop")

    async def stream(
        self, request: UnifiedChatRequest, agent: AgentBinding
    ) -> AsyncIterator[ProviderStreamEvent]:
        payload = self._chat_payload(request, agent)
        headers = self._get_headers()
        headers["Accept"] = "text/event-stream"

        try:
            async with self.client.client.stream(
                "POST", "/api/chat/v1/conversations", json=payload, headers=headers
            ) as response:
                response.raise_for_status()
                current_event: str | None = None
                total_chars = 0
                first_chunk_sent = False

                async for line in response.aiter_lines():
                    if line.startswith("event: "):
                        current_event = line[7:].strip()
                        continue
                    if not line:
                        if current_event == "DONE":
                            yield ProviderStreamEvent(type=ProviderStreamEventType.COMPLETED)
                            return
                        current_event = None
                        continue
                    if not line.startswith("data: ") or current_event != "data":
                        continue
                    try:
                        data = json.loads(line[6:])
                        text = data.get("v", "") if isinstance(data, dict) else ""
                        message_type = data.get("m") if isinstance(data, dict) else None
                        if message_type in {"thinking", "thought", "reasoning"} or not isinstance(text, str):
                            continue
                        total_chars += len(text)
                        if total_chars > self.settings.max_response_chars:
                            yield ProviderStreamEvent(
                                type=ProviderStreamEventType.ERROR,
                                error=ProviderError(
                                    "response_too_large",
                                    "Response exceeded configured limit.",
                                    False,
                                    "validation",
                                ),
                            )
                            return
                        if "<tool_call>" in text:
                            yield ProviderStreamEvent(
                                type=ProviderStreamEventType.ERROR,
                                error=ProviderError(
                                    "privategpt_agent_instruction_mismatch",
                                    "PrivateGPT emitted an unsupported tool call. Run: privategpt-adapter agent reset",
                                    False,
                                    "validation",
                                ),
                            )
                            return
                        if text:
                            first_chunk_sent = True
                            yield ProviderStreamEvent(type=ProviderStreamEventType.DELTA, text=text)
                    except json.JSONDecodeError:
                        continue
                yield ProviderStreamEvent(type=ProviderStreamEventType.COMPLETED)
        except BridgeError:
            raise
        except Exception:
            yield ProviderStreamEvent(
                type=ProviderStreamEventType.ERROR,
                error=ProviderError(
                    "upstream_error", "PrivateGPT stream failed.", is_retryable=False, category="upstream"
                ),
            )

    def _chat_payload(self, request: UnifiedChatRequest, agent: AgentBinding) -> dict[str, Any]:
        rendered_messages = "\n\n".join(
            f"[{message.role}]\n{message.content}" for message in request.messages
        )
        return {
            "parent_message_id": None,
            "conversation_id": None,
            "question": f"[MODE: Agent]\n\n{rendered_messages}",
            "metadata": {
                "attachments": [],
                "agent_id": agent.agent_id,
                "reasoning_effort": None,
            },
            "model_id": agent.internal_model_id,
            "tools": [],
        }
