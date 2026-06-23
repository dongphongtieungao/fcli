---
id: workspace-assumptions
title: Workspace Assumptions
type: assumptions
category: workspace_assumption
status: synthesized
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-05-13
updated: 2026-06-23
sensitivity: internal
truth_level: assumption
source_policy: workspace_only
confidence: medium
source_of_truth:
  - docs/grounding/manifest.md
verified_against:
  - AGENTS.md
related:
  - llm-wiki/index.md
  - llm-wiki/open-questions.md
  - llm-wiki/contradiction-log.md
upstream:
  - docs/grounding/manifest.md
downstream:
  - llm-wiki/tasks/current/
tags:
  - workspace/assumption
  - agent/context
---

# Assumptions

## Purpose

Record assumptions that are useful during a task but not yet official.

## Context

Assumptions must be verified before they drive code or official docs.

Each assumption should include its origin, confidence, validation owner when known, and a disposition such as active, verified, rejected, or promoted. A verified assumption becomes evidence only after the authoritative artifact is updated.

## Assumptions

| Date | Assumption | Source | Status |
|---|---|---|---|
| 2026-05-13 | The repository should keep reusable Spec-First/KMS scaffold while removing old domain content. | User correction | historical; retained for task traceability |

## Related

- [Workspace Index](./index.md)
- [Open Questions](./open-questions.md)
