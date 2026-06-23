---
id: task-implement-privategpt-bridge
title: Implement OpenCode PrivateGPT Bridge
type: task_memory
category: agent_workspace
status: synthesized
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: medium
source_of_truth:
  - Current user instruction, 2026-06-23
  - docs/01-requirements/00.requirement.md
related:
  - llm-wiki/tasks/current/raw.md
  - llm-wiki/tasks/current/intake.md
  - llm-wiki/tasks/current/analysis.md
  - llm-wiki/tasks/current/decision-log.md
  - llm-wiki/tasks/current/promotion.md
  - llm-wiki/tasks/current/verification-notes.md
verified_against:
  - docs/grounding/manifest.md
upstream:
  - docs/01-requirements/00.requirement.md
downstream:
  - src/
  - tests/
tags:
  - workspace/task
  - domain/privategpt-adapter
  - agent/implementation
---

# Implement OpenCode PrivateGPT Bridge

## Purpose

Track implementation of the application according to the published business requirements, architecture, BPMN, executable specifications, contracts, quality gates, and runbooks.

## Task Brief

Request summary: Implement code according to the repository's documented business behavior and design.

Task objective: Produce a working Python implementation of the OpenCode PrivateGPT Bridge without inventing behavior beyond official artifacts.

Task type: implementation with contract, integration, and acceptance-test verification.

Risk level: high because the task affects provider integration, streaming behavior, file handling, configuration, error mapping, and user-visible output.

Impacted domain/runtime model: `privategpt-adapter`, including shared configuration, API client, provider adapter, streaming normalization, and application entry points.

Artifact layer: requirement → architecture/BPMN → executable specification/contract → implementation → verification.

Expected output: Python source code, targeted tests, and verification evidence satisfying the documented MVP flows.

Files in scope: `src/`, `tests/`, configuration and packaging files required by the approved design, plus this task workspace.

Files out of scope: undocumented product features, changes to upstream OpenCode or PrivateGPT source, production deployment, secrets, and unrelated documentation rewrites.

Forbidden actions: Do not invent business rules, bypass published contracts, hardcode credentials, weaken validation, make destructive external calls, or treat `llm-wiki/` as source of truth.

Verification expectation: Resolve canonical commands from application spec, QA matrix, DoD, and runbooks; run targeted unit, BDD, contract, and smoke checks appropriate to implemented scope.

Related archived tasks: `llm-wiki/tasks/archive/20260623_001_repository-cleanup/` was archived before this task; it is not an implementation source.

Open questions: Exact implementation gaps and executable environment remain to be established through repository and artifact retrieval.

Ready status: confirmed local/API/provider implementation slice is complete and verified locally. Overall task verdict is `PARTIALLY VERIFIED`; browser OAuth, OS secure-store selection and real OpenCode/PrivateGPT wire compatibility remain blocked from completion claims.

Next skill: continue the implementation lane after the missing runtime/security evidence is supplied, then rerun evidence verification.

## Related

- [Raw Request](./raw.md)
- [Task Intake](./intake.md)
- [Analysis](./analysis.md)
- [Decision Log](./decision-log.md)
- [Promotion Notes](./promotion.md)
- [Verification Notes](./verification-notes.md)
