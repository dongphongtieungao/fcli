---
id: workspace-module-privategpt-adapter
title: PrivateGPT Adapter Module Note
type: module_note
category: workspace_module
status: verified
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
truth_level: observed
source_policy: workspace_only
confidence: high
source_of_truth:
  - docs/01-requirements/00.requirement.md
  - docs/01-requirements/00.requirement_Provider_SPI_Specification.md
  - docs/02-architecture/04.architecture-c4.md
verified_against:
  - src/privategpt_adapter/
  - tests/unit/
  - specs/10.openapi.yaml
related:
  - llm-wiki/codebase-map.md
  - llm-wiki/tasks/current/TASK.md
upstream:
  - docs/02-architecture/04.architecture-c4.md
downstream:
  - src/privategpt_adapter/
  - tests/unit/
tags:
  - workspace/module-note
  - domain/privategpt-adapter
  - agent/verification
---

# PrivateGPT Adapter Module Note

## Purpose

Record the currently observed Python module boundary and verification surface without replacing official architecture or requirements.

## Observed Boundary

`privategpt_adapter.api` depends on `AgentService`; `AgentService` depends on `ProviderRegistry` and the Provider SPI; only the composition root imports `PrivateGPTProvider`. The concrete provider owns upstream Model, Agent, Conversation and SSE details. The Bridge contains no filesystem, shell or tool executor.

## Implemented and Verified Locally

- Loopback-safe configuration and local Bearer API-key validation.
- `/health`, `/v1/models`, non-stream chat and OpenAI-compatible SSE framing.
- Single-model whitelist with rejection before upstream work.
- Typed Provider SPI, OpenCode-compatible Agent instructions, workspace Agent name/hash and in-process idempotent binding cache.
- PrivateGPT request mapping with mandatory `metadata.agent_id`, resolved internal model ID and `tools: []`.
- Observed PrivateGPT SSE event parsing, reasoning filtering, response bound and plugin-style tool-call rejection.
- One forced token refresh boundary after HTTP 401.

## Unverified Runtime Gates

- Browser OAuth callback/exchange and approved OS secure refresh-token store.
- Persistent Agent binding cache across process restarts.
- Redacted current PrivateGPT Model, Agent and Conversation/SSE captures.
- Installed OpenCode request payload for tools, attachments and streaming.
- Real cancellation/pilot evidence against PrivateGPT and OpenCode.

These gaps prevent a full-MVP or release-ready claim. See the official Application Spec and QA Matrix for authoritative gate status.

## Verification Surface

- `uv run pytest`
- `uv run --with ruff ruff check src tests`
- Local loopback process smoke for `/health` and `/v1/models`
- OpenAPI parse/path check against `specs/10.openapi.yaml`

## Related

- [Codebase Map](../codebase-map.md)
- [Current Task](../tasks/current/TASK.md)
- [Architecture](../../docs/02-architecture/04.architecture-c4.md)
- [QA Matrix](../../docs/06-quality-assurance/14.qa-matrix.md)
