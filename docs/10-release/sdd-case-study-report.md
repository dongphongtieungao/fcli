---
type: technical_note
category: runbook
status: draft
owner: unknown
domain: release
project: fcli
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
  - docs/09-runbook/18.dod.md
related:
  - ../methodology/methodology.md
  - ../grounding/manifest.md
  - ../09-runbook/18.dod.md
tags:
  - sdd/runbook
  - kms/metadata
  - sdd/reusable
  - sdd/generic
---

# SDD Case Study Report

## Purpose

Provide a reusable release and case-study framework for summarizing delivery context, decisions, evidence, risks, and lessons learned.

## Context

No project-specific case study has been written for the new project yet. Keep this document generic until real project evidence exists, then either fill it as a draft or create a versioned release/case-study note.

## Report Outline

| Section | Evidence To Add |
|---|---|
| Situation | Initial problem, constraints, and baseline state |
| Method | Planning, specification, implementation, and verification flow |
| Key Decisions | ADRs, trade-offs, and accepted alternatives |
| Verification | Commands, artifacts, review evidence, and residual risks |
| Lessons Learned | Reusable practices and follow-up improvements |

## Evidence Checklist

- Link to relevant requirements, architecture, and test artifacts.
- Record exact verification commands and outputs.
- Note unresolved questions and known limitations.
- Separate workspace observations from official conclusions.

## Release Evidence Shape

| Evidence | Description |
|---|---|
| Scope | What changed and what is excluded. |
| Version/build | Version, commit, package name, or build identifier. |
| Install/use guide | How a reviewer or operator can run the delivered artifact. |
| Verification | Commands, exit codes, logs, warnings, and output artifacts. |
| Data/security | Fixture handling, redaction, secret review, and data constraints. |
| Known issues | Accepted limitations and unresolved risks. |
| Rollback | How to revert, disable, or recover if release fails. |
| Approval | Human owner and decision status. |

## Promotion Rules

Promote release evidence when a package, handover, install guide, or acceptance decision needs to be auditable later. Keep noisy raw logs outside this document unless they are required evidence; link to artifact paths instead.

## Related

- [Methodology](../methodology/methodology.md)
- [Grounding Manifest](../grounding/manifest.md)
- [Definition of Done](../09-runbook/18.dod.md)
