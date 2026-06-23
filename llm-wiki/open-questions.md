---
id: workspace-open-questions
title: Workspace Open Questions
type: open_questions
category: workspace_question
status: observed
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
  - docs/01-requirements/00.requirement.md
related:
  - llm-wiki/index.md
  - llm-wiki/assumptions.md
  - llm-wiki/contradiction-log.md
upstream:
  - docs/grounding/manifest.md
downstream:
  - llm-wiki/tasks/current/
tags:
  - workspace/question
  - agent/context
---

# Open Questions

## Purpose

Track unresolved questions found during agent work.

## Context

Workspace notes are not official source of truth. Promote answers into `docs/` only when confirmed.

Questions must name their evidence source and owner when known. Closing a question here does not change official requirements; update the appropriate `docs/` artifact separately when a resolution becomes authoritative.

## Questions

| Date | Question | Source | Status |
|---|---|---|---|
| 2026-05-13 | What is the new project domain and first deliverable? | `docs/01-requirements/00.requirement.md` | resolved 2026-06-23 — the published requirement defines the `privategpt-adapter` domain and MVP scope |

## Related

- [Workspace Index](./index.md)
- [Assumptions](./assumptions.md)
- [Contradiction Log](./contradiction-log.md)
