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
related:
  - ../prompts.md
tags:
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - agent/planning
  - agent/implementation
---

# Application Runtime Prompt

## Purpose

Guide future runtime workflow design.

## Context

Use this prompt when a task affects runtime entry points, execution flow, logging, outputs, or operational behavior. It remains generic until the new project defines a real runtime contract.

## Instructions

- Identify entry points, configuration, logging, outputs, and failure modes.
- Keep runtime operations idempotent where possible.
- Avoid logging secrets or sensitive data.
- Verify behavior with a runnable smoke check when available.

## Maintenance Rules

- Promote stable runtime behavior into runbook, contract, or architecture docs.
- Record environment assumptions and failure modes when they become durable.
- Do not invent runtime behavior when requirements or contracts are missing.

## Related

- [Prompt routing](../prompts.md)
