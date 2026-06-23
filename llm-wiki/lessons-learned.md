---
id: workspace-lessons-learned
title: Workspace Lessons Learned
type: lessons_learned
category: workspace_lesson
status: synthesized
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-05-13
updated: 2026-06-23
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: medium
source_of_truth:
  - docs/grounding/manifest.md
verified_against:
  - llm-wiki/tasks/current/deletion-audit.md
related:
  - llm-wiki/index.md
  - llm-wiki/tasks/current/deletion-audit.md
upstream:
  - llm-wiki/tasks/current/
downstream: []
tags:
  - workspace/lesson
  - agent/context
---

# Lessons Learned

## Purpose

Record reusable lessons from agent work.

## Context

Lessons are workspace-only until promoted into official docs.

Record the originating task and evidence for each lesson. Promotion requires explicit review, a target under `docs/`, and any necessary update to the grounding registry.

## Lessons

| Date | Lesson | Source |
|---|---|---|
| 2026-05-13 | Cleanup should classify file role before deleting by keyword. | `tasks/current/deletion-audit.md` |

## Related

- [Workspace Index](./index.md)
- [Deletion Audit](./tasks/current/deletion-audit.md)
