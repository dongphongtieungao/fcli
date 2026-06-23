---
id: workspace-contradiction-log
title: Workspace Contradiction Log
type: contradiction_log
category: workspace_contradiction
status: observed
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-05-13
updated: 2026-06-23
sensitivity: internal
truth_level: observed
source_policy: workspace_only
confidence: high
source_of_truth:
  - docs/grounding/manifest.md
verified_against:
  - docs/grounding/manifest.yaml
  - docs/grounding/sources.md
related:
  - llm-wiki/index.md
  - llm-wiki/open-questions.md
  - llm-wiki/assumptions.md
upstream:
  - docs/grounding/manifest.md
downstream:
  - llm-wiki/tasks/current/
tags:
  - workspace/contradiction
  - risk/traceability
---

# Contradiction Log

## Purpose

Record contradictions between user instructions, docs, tests, workspace notes, and source code.

## Context

This file is workspace-only. Official resolution belongs in `docs/` when confirmed.

Record both conflicting sources, impact, decision owner, and resolution evidence. Do not silently select a winner or treat a row marked resolved as an official decision unless the resolution is reflected in the governing artifact.

## Contradictions

| Date | Conflict | Impact | Status |
|---|---|---|---|
| 2026-05-13 | Initial cleanup deleted reusable scaffold while user expected it to remain. | Required restoration and audit. | resolved in current task |
| 2026-06-22 | `requirements-index` previously described `docs/01-requirements/00.requirement.md` as a generic index while its content defined the OpenCode PrivateGPT Bridge MVP. | Could have caused agents to treat the adapter scope as draft or generic. | resolved — user designated the file as the published canonical requirement; `requirements-index` is retained only as a compatibility alias |

## Related

- [Workspace Index](./index.md)
- [Open Questions](./open-questions.md)
