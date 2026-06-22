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

# Application Interface Prompt

## Purpose

Guide future application interface design once the new project defines an application interface surface.

## Context

Use this prompt only after the project has or needs an application interface. It is a reusable design aid, not an active application contract.

## Instructions

- Keep interface names, inputs, outputs, and ownership explicit and documented.
- Define config precedence before implementation.
- Add validation and helpful errors for user-facing or system-facing inputs.
- Update specs and tests when application interface behavior becomes official.

## Maintenance Rules

- Promote stable application interface behavior into an application spec, API spec, UI spec, event contract, or schema.
- Keep examples synthetic and free of secrets.
- Do not treat this prompt as a canonical command definition.

## Related

- [Prompt routing](../prompts.md)
