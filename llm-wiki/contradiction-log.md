---
type: technical_note
status: draft
owner: unknown
domain: workspace
project: fcli
created: 2026-05-13
updated: 2026-06-22
sensitivity: internal
source_of_truth:
  - docs/grounding/manifest.md
related:
  - index.md
  - open-questions.md
tags:
  - workspace
  - contradictions
---

# Contradiction Log

## Purpose

Record contradictions between user instructions, docs, tests, workspace notes, and source code.

## Context

This file is workspace-only. Official resolution belongs in `docs/` when confirmed.

## Contradictions

| Date | Conflict | Impact | Status |
|---|---|---|---|
| 2026-05-13 | Initial cleanup deleted reusable scaffold while user expected it to remain. | Required restoration and audit. | resolved in current task |
| 2026-06-22 | `requirements-index` previously described `docs/01-requirements/00.requirement.md` as a generic index while its content defined the OpenCode PrivateGPT Bridge MVP. | Could have caused agents to treat the adapter scope as draft or generic. | resolved — user designated the file as the published canonical requirement; `requirements-index` is retained only as a compatibility alias |

## Related

- [Workspace Index](./index.md)
- [Open Questions](./open-questions.md)
