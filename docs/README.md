---
type: guide
category: governance
status: published
owner: unknown
domain: starter
project: fcli
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
related:
  - grounding/manifest.md
  - methodology/Task.md
  - 00-governance/metadata-schema.md
  - ../README.md
tags:
  - kms/governance
  - kms/guide
  - sdd/reusable
  - sdd/generic
---

# Documentation

## Purpose

Describe the reusable documentation system for projects created from this repository.

## Context

The repository currently keeps reusable governance, grounding, methodology, and template assets. Domain-specific project documents should be added only after the new project scope is known and should be linked through the grounding registry.

## Current Areas

| Path | Role |
|---|---|
| `00-governance/` | Metadata schema, taxonomy, and controlled values |
| `01-requirements/` | Requirement and use-case framework |
| `02-architecture/` | Vision, glossary, C4, NFR, ADR, standards, and structure |
| `03-business-analysis/` | Business glossary, BPMN, and analysis framework |
| `04-spec-test/` | Contract and spec governance material |
| `06-quality-assurance/` | QA matrix and prompt guardrails |
| `07-ci-cd-review/` | Quality matrix and review checklist |
| `09-runbook/` | DoR, DoD, and operational handover framework |
| `10-release/` | Release evidence and case-study framework |
| `grounding/` | Source registry, prompt routing, and grounding skills |
| `methodology/` | Delivery methodology and task workspace lifecycle |

## Reuse Rules

1. Keep active docs useful as frameworks, not empty placeholders.
2. Keep templates traceable to manifest, sources, and active artifact examples.
3. Promote workspace findings only when they become durable project knowledge.
4. Keep generated images, tmp notes, and raw logs out of official docs unless required as evidence.
5. Update `docs/grounding/manifest.yaml` and `sources.md` when a new official artifact becomes canonical.

## Promotion Targets

| Knowledge Type | Target |
|---|---|
| Requirement or acceptance criteria | `01-requirements/` |
| Architecture decision or boundary | `02-architecture/` |
| Business workflow | `03-business-analysis/` |
| API/application/schema contract | `04-spec-test/` or `specs/` |
| Verification rule | `06-quality-assurance/` |
| Review or release gate | `07-ci-cd-review/`, `09-runbook/`, `10-release/` |

## Related

- [Grounding manifest](./grounding/manifest.md)
- [Task workspace lifecycle](./methodology/Task.md)
- [Metadata Schema](./00-governance/metadata-schema.md)
- [Root README](../README.md)
