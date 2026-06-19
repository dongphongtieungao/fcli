---
type: technical_note
category: grounding
status: draft
owner: unknown
domain: grounding
project: unknown
created: YYYY-MM-DD
updated: YYYY-MM-DD
sensitivity: internal
source_of_truth:
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
related:
  - manifest.md
  - prompts.md
tags:
  - kms/grounding
  - kms/metadata
  - sdd/reusable
  - sdd/generic
  - sdd/template
---

# Evaluation Template

## Purpose

Define evaluation criteria for grounded agent behavior or project outputs.

## Context

Use this when prompts, contracts, or acceptance criteria need repeatable checks.

## Evaluation Areas

| Area | Metric | Method | Status |
|---|---|---|---|
| Grounding | Source IDs resolve | Catalog check | Draft |
| Faithfulness | Answer follows sources | Review or automated eval | Draft |
| Safety | No sensitive data exposure | Review or scanner | Draft |

## Related

- [manifest.md](./manifest.md)
- [prompts.md](./prompts.md)
