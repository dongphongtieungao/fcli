"""OpenAI-compatible request and response schemas."""

from pydantic import BaseModel, Field


class OpenAIMessage(BaseModel):
    role: str
    content: str


class OpenAIChatRequest(BaseModel):
    model: str
    messages: list[OpenAIMessage]
    stream: bool = False
    temperature: float | None = None
    top_p: float | None = None
    max_tokens: int | None = None
    tools: list[dict] | None = None
    tool_choice: str | dict | None = None


class OpenAIChoice(BaseModel):
    index: int
    message: OpenAIMessage
    finish_reason: str | None = None


class OpenAIChatResponse(BaseModel):
    id: str
    object: str = "chat.completion"
    created: int
    model: str
    choices: list[OpenAIChoice]
    usage: dict | None = None


class OpenAIDelta(BaseModel):
    role: str | None = None
    content: str | None = None


class OpenAIStreamChoice(BaseModel):
    index: int
    delta: OpenAIDelta
    finish_reason: str | None = None


class OpenAIStreamResponse(BaseModel):
    id: str
    object: str = "chat.completion.chunk"
    created: int
    model: str
    choices: list[OpenAIStreamChoice]
