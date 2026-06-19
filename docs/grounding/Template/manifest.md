---
type: manifest
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
  - manifest.yaml
  - sources.md
  - prompts.md
tags:
  - kms/grounding
  - sdd/reusable
  - sdd/generic
  - sdd/template
---

# Grounding Manifest Template

## Purpose

Provide a human-readable source map for project knowledge.

## Context

Use this file to explain source precedence, official artifacts, tests, workspace notes, and implementation evidence.

## Source Precedence

| Priority | Source | Role |
|---|---|---|
| 1 | Current user instruction | Immediate task intent |
| 2 | Agent policy | Operating contract |
| 3 | Official docs | Source of truth |
| 4 | Tests/specs | Executable evidence |
| 5 | Workspace notes | Working memory |
| 6 | Source code | Current implementation |

## Coverage

| Area | Paths | Status |
|---|---|---|
| TBD | TBD | Draft |

## Related

- [manifest.yaml](./manifest.yaml)
- [sources.md](./sources.md)
- [prompts.md](./prompts.md)
