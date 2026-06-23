---
id: task-sync-code-requirement-architecture-decisions
title: Decision Log — Code and Documentation Synchronization
type: task_memory
status: draft
owner: agent
domain: privategpt-adapter
project: fcli
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
source_of_truth:
  - docs/01-requirements/00.requirement.md
  - docs/02-architecture/04.architecture-c4.md
related:
  - llm-wiki/tasks/current/TASK.md
  - llm-wiki/tasks/current/analysis.md
tags:
  - workspace/task
  - decisions
---

# Decision Log — Code and Documentation Synchronization

## Purpose

Capture only decisions grounded in official artifacts, tests, or explicit user direction.

## Context

### D-001 — Implement only observed PrivateGPT HTTP boundaries

Decision: replace the mock upstream with the Model discovery, Agent discovery/create/update, and Conversation SSE endpoints observed in the published Java plugin reference.

Source: canonical requirement §2.5, FR004–FR010, Provider SPI appendix, and the cited plugin source.

Impact: the local API and services remain Provider-SPI based; provider-specific endpoints remain isolated in `src/providers/privategpt.py`.

### D-002 — Preserve request messages; keep a stable Agent instruction

Decision: system/developer/user messages remain in the mapped conversation, while the workspace Agent uses the fixed OpenCode-compatible instruction profile.

Source: FR006, FR008, Application Spec “Chat completion”.

Impact: request context is not dropped and per-request system text does not mutate persistent Agent instructions.

### D-003 — Do not implement unconfirmed OAuth or secure persistence

Decision: keep access-token handling in memory and do not invent OAuth callback, token exchange, refresh, or OS credential-store behavior.

Source: requirement §16 and Application Spec open questions.

Impact: the product remains incomplete for login/refresh requirements; this is a tracked gate, not a silent fallback.

## Related

- [Task](./TASK.md)
- [Analysis](./analysis.md)
