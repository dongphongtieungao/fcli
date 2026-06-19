---
type: guide
status: draft
owner: unknown
domain: starter
project: ftransform
created: 2026-04-27
updated: 2026-05-13
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/AGENTS.md
  - docs/grounding/manifest.md
related:
  - docs/README.md
  - docs/grounding/manifest.md
  - docs/methodology/Task.md
  - llm-wiki/tasks/current/TASK.md
tags:
  - starter
  - normalized
---

# Project Starter

## Purpose

This repository is a cleaned starter structure for a new project. It keeps the reusable Spec-First, KMS, grounding, and task-workspace conventions while removing old domain-specific implementation and documentation.

## Context

The previous project-specific source code and most related documentation have been removed. New project behavior, architecture, contracts, tests, and runbooks should be added deliberately as the new scope becomes clear.

## Structure

| Path | Purpose |
|---|---|
| `src/` | New application code |
| `tests/` | Automated tests and executable examples |
| `docs/` | Official project documentation |
| `docs/grounding/` | Grounding manifest, sources, and prompt routing |
| `docs/methodology/` | Delivery and agent workflow methodology |
| `llm-wiki/` | Agent workspace notes and active task memory |
| `specs/` | Machine-readable contracts and schemas |
| `tools/` | Local helper scripts |

## Starter Workflow

1. Define the new project objective in `docs/` or `llm-wiki/tasks/current/`.
2. Add the smallest useful architecture, contract, test, or runbook artifact.
3. Implement code in `src/`.
4. Verify with targeted checks.
5. Promote durable learnings from `llm-wiki/` into `docs/` only when they become official.

## Related

- [docs README](./docs/README.md)
- [grounding manifest](./docs/grounding/manifest.md)
- [task workspace methodology](./docs/methodology/Task.md)
- [current task](./llm-wiki/tasks/current/TASK.md)
# fcli
