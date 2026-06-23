"""Agent Lifecycle Service — with persistent cache and clear fail-fast behaviour.

FR005: Agent sync per workspace; cache agent_id, model_id, instructions_hash.
FR006: OpenCode-compatible instruction built and hashed here.
Policy: Agent sync failure must raise — no silent fallback.
"""

from __future__ import annotations

import hashlib
import logging

from src.providers.registry import ProviderRegistry
from src.providers.spi import AgentSyncRequest
from src.schemas.unified import AgentBinding
from src.services.agent_cache import AgentCache

logger = logging.getLogger(__name__)


class AgentService:
    def __init__(
        self,
        registry: ProviderRegistry,
        workspace_key: str,
        agent_cache: AgentCache | None = None,
    ):
        self.registry = registry
        self.workspace_key = workspace_key
        self._cache: AgentCache = agent_cache or AgentCache()
        # Hot in-memory binding avoids a disk read on every request.
        self._hot_binding: AgentBinding | None = None

    # ------------------------------------------------------------------
    # Instruction builder — FR006
    # ------------------------------------------------------------------

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

    def _hash_instructions(self, instructions: str) -> str:
        return hashlib.sha256(instructions.encode()).hexdigest()

    def _agent_name(self) -> str:
        short = hashlib.sha256(self.workspace_key.encode()).hexdigest()[:12]
        return f"PrivateGPT-Adapter-OpenCode-{short}"

    # ------------------------------------------------------------------
    # Core lifecycle
    # ------------------------------------------------------------------

    async def ensure_agent(self, provider_name: str, internal_model_id: str) -> AgentBinding:
        """Return a valid AgentBinding, syncing with PrivateGPT if needed.

        Cache lookup order:
        1. Hot in-memory binding (same model + same instruction hash → skip network).
        2. Persistent disk cache (survives restarts; still validates hash + model).
        3. Remote sync via PrivateGPT Agent API.

        Raises BridgeError on sync failure — no silent fallback (policy FR005 AC #6).
        """
        instructions = self.build_instructions()
        instructions_hash = self._hash_instructions(instructions)

        # 1. Hot cache
        if self._is_valid(self._hot_binding, internal_model_id, instructions_hash):
            return self._hot_binding  # type: ignore[return-value]

        # 2. Persistent disk cache
        disk_binding = self._cache.load(self.workspace_key)
        if self._is_valid(disk_binding, internal_model_id, instructions_hash):
            self._hot_binding = disk_binding
            return disk_binding  # type: ignore[return-value]

        # 3. Remote sync
        logger.info("Syncing PrivateGPT Agent for workspace %s ...", self.workspace_key)
        provider = self.registry.get_provider(provider_name)
        request = AgentSyncRequest(
            workspace_key=self.workspace_key,
            instructions_hash=instructions_hash,
            internal_model_id=internal_model_id,
            name=self._agent_name(),
            instructions=instructions,
        )
        binding = await provider.ensure_agent(request)
        # Persist to disk and hot cache
        self._cache.save(binding)
        self._hot_binding = binding
        logger.info("Agent synced: agent_id=%s", binding.agent_id)
        return binding

    async def reset(self) -> None:
        """Clear all cached state for this workspace (agent reset command)."""
        self._hot_binding = None
        self._cache.delete(self.workspace_key)

    def status(self) -> dict:
        """Return safe status dict for CLI agent status command."""
        return self._cache.get_status(self.workspace_key)

    # ------------------------------------------------------------------
    # Helpers
    # ------------------------------------------------------------------

    @staticmethod
    def _is_valid(
        binding: AgentBinding | None,
        internal_model_id: str,
        instructions_hash: str,
    ) -> bool:
        return (
            binding is not None
            and binding.internal_model_id == internal_model_id
            and binding.instructions_hash == instructions_hash
        )
