"""Agent Lifecycle Service."""

import hashlib

from src.providers.registry import ProviderRegistry
from src.providers.spi import AgentSyncRequest
from src.schemas.unified import AgentBinding


class AgentService:
    def __init__(self, registry: ProviderRegistry, workspace_key: str):
        self.registry = registry
        self.workspace_key = workspace_key
        self._cached_binding: AgentBinding | None = None

    def _hash_instructions(self, instructions: str) -> str:
        return hashlib.sha256(instructions.encode()).hexdigest()

    @staticmethod
    def build_instructions() -> str:
        """Return the stable Agent instruction required by FR006.

        Request-specific system and developer messages are deliberately kept in
        the conversation payload; they must not mutate the workspace Agent.
        """
        return "\n".join(
            (
                "You are the model backend for OpenCode CLI.",
                "OpenCode is responsible for workspace access, file reading, file editing, command execution and user approval.",
                "Do not emit IntelliJ plugin style <tool_call> tags.",
                "Do not claim that files were changed unless OpenCode provides tool result context.",
                "Follow OpenCode system messages and the user's task.",
                "Return responses compatible with OpenCode.",
            )
        )

    async def ensure_agent(self, provider_name: str, internal_model_id: str) -> AgentBinding:
        provider = self.registry.get_provider(provider_name)
        instructions = self.build_instructions()
        instructions_hash = self._hash_instructions(instructions)
        
        if (
            self._cached_binding
            and self._cached_binding.internal_model_id == internal_model_id
            and self._cached_binding.instructions_hash == instructions_hash
        ):
            return self._cached_binding
            
        request = AgentSyncRequest(
            workspace_key=self.workspace_key,
            instructions_hash=instructions_hash,
            internal_model_id=internal_model_id,
            name=f"PrivateGPT-Adapter-OpenCode-{hashlib.sha256(self.workspace_key.encode()).hexdigest()[:12]}",
            instructions=instructions,
        )
        self._cached_binding = await provider.ensure_agent(request)
        return self._cached_binding
