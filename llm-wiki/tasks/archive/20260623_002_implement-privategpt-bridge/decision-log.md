---
id: task-implement-privategpt-bridge-decisions
title: Implementation Decision Log
type: task_memory
category: agent_workspace
status: observed
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: low
source_of_truth:
  - docs/02-architecture/06.adr.md
related:
  - llm-wiki/tasks/current/TASK.md
  - llm-wiki/tasks/current/analysis.md
verified_against: []
upstream:
  - docs/02-architecture/06.adr.md
downstream:
  - src/
tags:
  - workspace/task
  - agent/decision
---

# Implementation Decision Log

## Purpose

Record task-level implementation decisions and their official evidence without replacing the ADR.

## Decisions

| Decision | Source | Consequence |
|---|---|---|
| Replace the broken `app.*` skeleton with the installable `privategpt_adapter` package. | ADR-001, C4, existing skeleton inspection. | Runtime imports and CLI packaging become executable; old placeholder paths are removed. |
| Implement only the confirmed local/API/provider slice and retain explicit gates for OAuth, secure storage and real wire compatibility. | Application Spec implementation gates; DoR `READY_WITH_GAPS`. | No invented auth endpoints, token persistence or OpenCode/PrivateGPT payload claims. |
| Keep concrete-provider wiring in the composition root; API/service depend on SPI/registry. | Provider SPI; STD-002; NFR-MAIN-001. | Dependency direction is testable and future provider changes remain isolated. |
| Use an in-memory access-token provider for the verified slice. | FR002–FR003 access-token memory rule; secure-store decision remains open. | Local mocked/provider tests work without plaintext persistence; browser login is not claimed implemented. |

## Related

- [Current Task](./TASK.md)
- [Analysis](./analysis.md)
- [Architecture Decisions](../../../docs/02-architecture/06.adr.md)
