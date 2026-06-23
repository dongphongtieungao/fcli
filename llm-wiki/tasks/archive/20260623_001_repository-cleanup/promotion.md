---
type: technical_note
status: draft
owner: unknown
domain: workspace
project: fcli
created: 2026-05-13
updated: 2026-05-13
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/grounding/skills/9.create-current-task.md
  - docs/methodology/Task.md
related:
  - TASK.md
  - decision-log.md
tags:
  - workspace/task
  - promotion
---

# Promotion Notes

## Purpose

Track whether cleanup findings should be promoted into official project documentation.

## Context

Task creation itself does not promote workspace notes into `docs/`.

## Promotion Plan

Promotion needed: completed for the starter entrypoints changed in this task.

Reason: The repository currently contains project-specific source-of-truth documents. If they are changed, official docs and catalogs may need to stay internally consistent.

Target artifact layer: agent policy, guide, grounding, prompt governance, source catalog, methodology support notes, and workspace notes.

Target path: `AGENTS.md`, `README.md`, `docs/README.md`, `docs/grounding/*`, selected `docs/methodology/*`, and selected skill docs.

Manifest update needed: completed by rewriting `docs/grounding/manifest.md` and `docs/grounding/manifest.yaml` for starter scope.

Sources update needed: completed by rewriting `docs/grounding/sources.md`.

Prompts update needed: completed by rewriting `docs/grounding/prompts.md` and generic prompt files.

## Related

- [TASK.md](./TASK.md)
- [decision-log.md](./decision-log.md)
