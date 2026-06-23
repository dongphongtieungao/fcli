"""Provider Service Provider Interface (SPI)."""

from typing import AsyncIterator, Protocol, Sequence

from src.schemas.unified import (
    AgentBinding,
    ProviderStreamEvent,
    UnifiedChatRequest,
    UnifiedChatResponse,
)


class ProviderModel:
    def __init__(self, id: str, name: str):
        self.id = id
        self.name = name


class ProviderHealth:
    def __init__(self, status: str):
        self.status = status


class AgentSyncRequest:
    def __init__(
        self,
        workspace_key: str,
        instructions_hash: str,
        internal_model_id: str,
        name: str,
        instructions: str,
    ):
        self.workspace_key = workspace_key
        self.instructions_hash = instructions_hash
        self.internal_model_id = internal_model_id
        self.name = name
        self.instructions = instructions


class ProviderAdapter(Protocol):
    async def initialize(self) -> None:
        ...

    async def health_check(self) -> ProviderHealth:
        ...

    async def list_models(self) -> Sequence[ProviderModel]:
        ...

    async def resolve_model(self, public_model: str) -> ProviderModel:
        ...

    async def ensure_agent(self, request: AgentSyncRequest) -> AgentBinding:
        ...

    async def complete(self, request: UnifiedChatRequest, agent: AgentBinding) -> UnifiedChatResponse:
        ...

    def stream(self, request: UnifiedChatRequest, agent: AgentBinding) -> AsyncIterator[ProviderStreamEvent]:
        ...
