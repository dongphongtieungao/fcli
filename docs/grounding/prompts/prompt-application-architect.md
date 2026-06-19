---
type: prompt_governance
category: prompt_governance
status: published
owner: unknown
domain: grounding
project: ftransform
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - docs/grounding/prompts.md
  - docs/grounding/sources.md
related:
  - ../prompts.md
  - prompt-application-coder.md
  - prompt-application-debugger.md
tags:
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - agent/planning
  - agent/implementation
---

# Planning Prompt

## Purpose

Guide planning, architecture, and trade-off analysis for the new project.

## Context

Use this prompt when a task needs structured planning before implementation, especially when scope, architecture, risk, or source-of-truth impact is unclear.

## Instructions

1. Read `AGENTS.md`, local policy, and the relevant official docs first.
2. Identify scope, assumptions, risks, files in scope, files out of scope, and verification expectations.
3. Prefer small implementation steps and explicit acceptance criteria.
4. Do not invent domain rules before the new project defines them.

## Maintenance Rules

- Promote durable architecture decisions into ADR or C4 docs.
- Keep exploratory trade-offs in task notes until accepted.
- Link new planning rules back to manifest, sources, and Task methodology.

## Related

- [Prompt routing](../prompts.md)
- [Implementation prompt](./prompt-application-coder.md)
- [Debugging prompt](./prompt-application-debugger.md)
