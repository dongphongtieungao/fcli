"""Persistent workspace Agent cache backed by a JSON file.

Stores agent_id, model_id, instructions_hash and updated_at per workspace_key
so the binding survives adapter restarts (FR005 AC #4, NFR 9.3 #2).

Cache location: ~/.privategpt-adapter/agent_cache.json
"""

from __future__ import annotations

import json
import logging
import os
import time
from dataclasses import asdict, dataclass
from pathlib import Path

from src.schemas.unified import AgentBinding

logger = logging.getLogger(__name__)

_DEFAULT_CACHE_DIR = Path.home() / ".privategpt-adapter"
_CACHE_FILE = "agent_cache.json"


def _cache_path(cache_dir: Path = _DEFAULT_CACHE_DIR) -> Path:
    return cache_dir / _CACHE_FILE


@dataclass
class _CacheEntry:
    workspace_key: str
    agent_id: str
    internal_model_id: str
    instructions_hash: str
    updated_at: float  # unix timestamp


class AgentCache:
    """Read/write persistent agent binding keyed by workspace_key."""

    def __init__(self, cache_dir: Path | None = None):
        self._cache_dir = cache_dir or _DEFAULT_CACHE_DIR
        self._cache_file = _cache_path(self._cache_dir)

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    def load(self, workspace_key: str) -> AgentBinding | None:
        """Return cached AgentBinding for this workspace, or None."""
        entries = self._read_all()
        entry_data = entries.get(workspace_key)
        if not entry_data:
            return None
        try:
            entry = _CacheEntry(**entry_data)
            return AgentBinding(
                workspace_key=entry.workspace_key,
                agent_id=entry.agent_id,
                internal_model_id=entry.internal_model_id,
                instructions_hash=entry.instructions_hash,
            )
        except (TypeError, KeyError) as exc:
            logger.debug("Ignoring invalid cache entry for %s: %s", workspace_key, exc)
            return None

    def save(self, binding: AgentBinding) -> None:
        """Persist an AgentBinding to the cache file."""
        entries = self._read_all()
        entries[binding.workspace_key] = asdict(
            _CacheEntry(
                workspace_key=binding.workspace_key,
                agent_id=binding.agent_id,
                internal_model_id=binding.internal_model_id,
                instructions_hash=binding.instructions_hash,
                updated_at=time.time(),
            )
        )
        self._write_all(entries)
        logger.debug("Agent cache updated for workspace %s.", binding.workspace_key)

    def delete(self, workspace_key: str) -> None:
        """Remove the cached entry for this workspace (agent reset)."""
        entries = self._read_all()
        removed = entries.pop(workspace_key, None)
        if removed:
            self._write_all(entries)
            logger.info("Agent cache cleared for workspace %s.", workspace_key)
        else:
            logger.debug("No cache entry found for workspace %s.", workspace_key)

    def get_status(self, workspace_key: str) -> dict:
        """Return a safe status dict for the CLI agent status command."""
        entries = self._read_all()
        entry_data = entries.get(workspace_key)
        if not entry_data:
            return {"workspace_key": workspace_key, "status": "not_synced"}
        return {
            "workspace_key": workspace_key,
            "status": "synced",
            "agent_id": entry_data.get("agent_id"),
            "internal_model_id": entry_data.get("internal_model_id"),
            "instructions_hash": entry_data.get("instructions_hash"),
            "updated_at": entry_data.get("updated_at"),
        }

    # ------------------------------------------------------------------
    # Private helpers
    # ------------------------------------------------------------------

    def _read_all(self) -> dict:
        if not self._cache_file.exists():
            return {}
        try:
            return json.loads(self._cache_file.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError) as exc:
            logger.warning("Could not read agent cache: %s", exc)
            return {}

    def _write_all(self, entries: dict) -> None:
        try:
            self._cache_dir.mkdir(parents=True, exist_ok=True)
            self._cache_file.write_text(
                json.dumps(entries, indent=2), encoding="utf-8"
            )
        except OSError as exc:
            logger.warning("Could not write agent cache: %s", exc)
