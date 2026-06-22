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
  - docs/grounding/sources.md
related:
  - ../prompts.md
  - prompt-application-architect.md
  - prompt-application-debugger.md
tags:
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - agent/planning
  - agent/implementation
---

# Implementation Prompt

## Purpose

Guide code and test implementation for the new project.

## Context

Use this prompt after the relevant requirement, contract, architecture, or task brief is clear enough for implementation. It must not be used to invent missing business rules.

## Instructions

1. Read policy, contracts, and relevant source files before editing.
2. Keep changes scoped to the requested behavior.
3. Add or update tests when behavior changes.
4. Avoid hardcoded secrets and sensitive data in code, logs, fixtures, and docs.
5. Verify with the smallest useful command and report the evidence.

## Maintenance Rules

- Promote reusable implementation constraints into standards or review checklists.
- Keep one-off implementation notes in task workspace.
- Do not use this prompt to bypass requirement, contract, or verification gaps.

## Related

- [Prompt routing](../prompts.md)
- [Planning prompt](./prompt-application-architect.md)
- [Debugging prompt](./prompt-application-debugger.md)
