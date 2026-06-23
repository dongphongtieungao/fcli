---
id: task-implement-privategpt-bridge-analysis
title: Implementation Analysis
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
confidence: high
source_of_truth:
  - docs/grounding/manifest.md
related:
  - llm-wiki/tasks/current/TASK.md
  - llm-wiki/tasks/current/intake.md
verified_against:
  - docs/01-requirements/00.requirement.md
  - docs/01-requirements/00.requirement_Provider_SPI_Specification.md
  - docs/02-architecture/04.architecture-c4.md
  - docs/04-spec-test/application-spec.md
  - specs/10.openapi.yaml
  - tests/features/
upstream:
  - llm-wiki/tasks/current/intake.md
downstream:
  - llm-wiki/tasks/current/decision-log.md
tags:
  - workspace/task
  - agent/analysis
---

# Implementation Analysis

## Purpose

Record grounded findings, boundaries, gaps, and implementation sequencing for this task.

## Initial State

The repository contains a non-runnable Python skeleton. Imports target a missing `app` package, the provider returns a mock response, `/v1/models` and local authentication are absent, and no executable Python tests exist. Official documentation is ahead of code by design.

## Findings

### Context Pack

- Official sources: canonical requirement and Provider SPI appendix; C4/NFR/ADR/Standards; BPMN/use cases; application/OpenAPI contracts; QA Matrix; DoR/DoD.
- Executable acceptance: four Gherkin feature files cover UC-001 through UC-006, including model rejection, Agent sync, streaming, cancellation and tool isolation.
- Current source: `src/` has API, service, provider, HTTP and resilience placeholders but no complete package or runtime wiring.
- Contradiction: no business-source conflict found. The documented state `docs_ahead_of_code` matches the observed skeleton.
- Missing runtime evidence: browser OAuth endpoints, approved secure-store implementation, exact installed OpenCode payload, current Agent/Conversation/SSE schemas and error/status mapping.

### Prompt Routing

- Primary lane: implementation.
- Secondary lanes: testing, verification and workspace update.
- Prompt contracts: `prompt-application-coder.md`, `application-runtime.md`, `application-config.md`, and `application-validation.md`.
- Forbidden: tool execution, filesystem access in the Bridge, plaintext secrets, model fallback, invented wire compatibility and overclaiming verification.

### Spec Acceptance Map

| Acceptance slice | Sources | Planned evidence | Gate |
|---|---|---|---|
| Loopback API and health | FR001, APP-HTTP-001, QA-F001/F002 | application/config tests | ready |
| Model list and rejection | FR004, OpenAPI, QA-F005/F006 | unit and API contract tests | ready |
| Provider SPI and Agent requirement | FR005–FR008, Provider SPI, QA-F008/F009 | fake-provider and request-mapping tests | ready with mocked upstream |
| Non-stream response | FR007–FR010, OpenAPI, QA-F010 | service/API contract tests | ready with mocked provider |
| SSE framing and `[DONE]` | FR009–FR010, QA-F011/F012 | parser/mapper fixtures based on observed plugin events | ready with gap; not runtime-wire verified |
| Tool isolation | FR012, NFR-SEC-003, QA-F015/F016 | validation and dependency tests | confirmed safe subset only |
| Browser login and secure persistence | FR002–FR003, ADR-008 | platform/runtime integration | blocked pending endpoints and secure-store decision |
| Real OpenCode/PrivateGPT pilot | FR011–FR013 | redacted captures and smoke/pilot | blocked outside local test environment |

### Code Boundary Result

- Target boundary: local API → Agent Service → Provider SPI → PrivateGPT provider/HTTP.
- API and service must not import the concrete provider; concrete registration is confined to composition/wiring.
- Provider owns PrivateGPT Model/Agent/Conversation HTTP and upstream SSE parsing.
- API owns OpenAI request validation and SSE framing.
- Tests use fake providers and `httpx.MockTransport`; no external calls or secrets.
- Implementation readiness: `READY_WITH_GAPS` for the confirmed local/API/provider slice. Blocked capabilities remain explicitly unclaimed.

## Related

- [Current Task](./TASK.md)
- [Task Intake](./intake.md)
- [Decision Log](./decision-log.md)
