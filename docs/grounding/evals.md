---
type: technical_note
category: grounding
status: published
owner: unknown
domain: grounding
project: ftransform
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
related:
  - manifest.md
  - prompts.md
  - sources.md
  - ../06-quality-assurance/14.qa-matrix.md
tags:
  - kms/grounding
  - kms/metadata
  - sdd/reusable
  - sdd/generic
---

# Evaluation Notes

## Purpose

Define reusable evaluation criteria for grounding quality, prompt behavior, documentation traceability, and agent completion claims.

## Context

No domain-specific eval suite is active in this starter repository. The checks below evaluate whether the reusable KMS and agent workflow remain usable as project-specific artifacts are added.

## Evaluation Dimensions

| Dimension | What To Check | Evidence |
|---|---|---|
| Source registry | Source IDs exist, paths resolve, and coverage is meaningful. | YAML parse, path check |
| Prompt routing | Prompt lanes point to real prompts and require relevant sources. | Prompt review |
| Documentation traceability | Important docs have source, related links, and no orphan status. | Link/metadata review |
| Verification discipline | Claims distinguish verified evidence, partial evidence, and gaps. | QA/DoD review |
| Reuse quality | Starter docs are frameworks, not empty placeholders. | Content review |
| Domain neutrality | Old project terms do not bias new project behavior. | Targeted legacy scan |

## Starter Eval Cases

| ID | Check | Pass Criteria |
|---|---|---|
| EVAL-001 | Manifest parses and source paths exist. | No parse errors, no missing source paths. |
| EVAL-002 | Active docs have Purpose, Context, and Related/References. | No important orphan docs. |
| EVAL-003 | Prompt files link to prompt governance and relevant sources. | No broken prompt links. |
| EVAL-004 | Verification notes report commands, exit code, warnings, and verdict when commands run. | Evidence is reproducible. |
| EVAL-005 | Reusable docs contain principles, decision rules, checklists, or promotion guidance. | No empty official registry/framework docs. |

## Promotion Rules

Promote eval cases when they should become recurring QA gates. Keep one-off audit findings in task verification notes unless they become reusable checks.

## Related

- [manifest.md](./manifest.md)
- [prompts.md](./prompts.md)
- [sources.md](./sources.md)
- [QA Matrix](../06-quality-assurance/14.qa-matrix.md)
