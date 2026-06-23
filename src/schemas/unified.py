"""Unified contracts to isolate API layer from provider specifics."""

from dataclasses import dataclass, field
from enum import StrEnum
from typing import Mapping, Sequence


@dataclass(frozen=True)
class UnifiedMessage:
    role: str
    content: str


@dataclass(frozen=True)
class UnifiedChatRequest:
    public_model: str
    messages: Sequence[UnifiedMessage]
    stream: bool
    metadata: Mapping[str, object] = field(default_factory=dict)


@dataclass(frozen=True)
class AgentBinding:
    workspace_key: str
    agent_id: str
    internal_model_id: str
    instructions_hash: str


@dataclass(frozen=True)
class UnifiedChatResponse:
    content: str
    finish_reason: str | None = None
    usage: dict | None = None
    metadata: dict | None = None


class ProviderStreamEventType(StrEnum):
    DELTA = "delta"
    COMPLETED = "completed"
    ERROR = "error"


@dataclass(frozen=True)
class ProviderError(Exception):
    code: str
    message: str
    is_retryable: bool
    category: str


@dataclass(frozen=True)
class ProviderStreamEvent:
    type: ProviderStreamEventType
    text: str | None = None
    finish_reason: str | None = None
    error: ProviderError | None = None
