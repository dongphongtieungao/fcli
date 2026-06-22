---
type: prompt_governance
category: prompt_governance
status: published
owner: unknown
domain: grounding
project: fcli
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - docs/grounding/prompts.md
related:
  - ../prompts.md
tags:
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - agent/planning
  - agent/implementation
---

# Application Validation Prompt

## Purpose

Guide future validation design for application and batch workflows.

## Context

Use this prompt when a task needs validation evidence for an application, batch, script, or generated artifact workflow. It does not define a canonical command by itself.

## Instructions

- Resolve the canonical validation source before claiming a pass.
- Record exact commands, exit code, warnings, and artifacts.
- Treat warnings and partial output as partial verification unless official docs say otherwise.
- Keep validation checks reproducible.

## Maintenance Rules

- Promote reusable validation commands into QA matrix, DoD, or runbook.
- Keep one-off command output in task verification notes.
- Do not call a check fully verified when warnings, missing artifacts, or environment gaps remain unexplained.

## Related

- [Prompt routing](../prompts.md)
