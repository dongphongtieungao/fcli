---
type: technical_note
category: governance
status: published
owner: unknown
domain: governance
project: fcli
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - AGENTS.md
related:
  - ../00.governance-rules.md
tags:
  - kms/governance
  - kms/metadata
  - sdd/reusable
  - sdd/generic
  - risk/security
---

# Base Governance Rules

## Purpose

Provide reusable governance rule structure.

## Context

Use this file to bootstrap project-specific governance rules without inheriting old project content.

## Rule Format

| Field | Meaning |
|---|---|
| ID | Stable identifier |
| Rule | Enforceable statement |
| Severity | Blocker, major, minor, or info |
| Scope | Files or behavior affected |
| Verification | How the rule is checked |

## Related

- [Governance Rules](../00.governance-rules.md)
