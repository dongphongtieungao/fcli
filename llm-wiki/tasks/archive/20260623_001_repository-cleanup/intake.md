---
type: technical_note
status: draft
owner: unknown
domain: workspace
project: fcli
created: 2026-05-13
updated: 2026-05-13
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/grounding/skills/9.create-current-task.md
  - docs/methodology/Task.md
related:
  - TASK.md
  - raw.md
  - analysis.md
tags:
  - workspace/task
  - intake
---

# Task Intake

## Purpose

Convert the raw request into an actionable cleanup brief.

## Context

This task starts a project reset. The existing repository contract still contains old domain-specific constraints, so cleanup must distinguish reusable process structure from old implementation and documentation.

## Structured Brief

Objective: Clean remaining legacy old-project content so the repo can become a starter structure for a new project.

Task type: repository cleanup and documentation cleanup.

Risk level: medium. Deleting old code/docs can remove useful starter scaffolding or violate documentation traceability if done without inventory.

Impacted report model: shared / n/a.

Artifact layer: workspace-only now; likely docs/KMS cleanup later if official docs are modified.

Expected output: Cleaned repo plus evidence of remaining old-project references or confirmation that obvious references were removed.

Likely sources to read:

- `AGENTS.md`
- `docs/AGENTS.md` if editing `docs/`
- `docs/grounding/manifest.md`
- `docs/grounding/manifest.yaml`
- Repository file inventory from `rg --files`
- Old-project reference scans for legacy domain terms, old mode names, and project-specific paths

Verification expectation:

- Run targeted `rg` scans before and after cleanup.
- Report files deleted, edited, or left intentionally.
- For Markdown edits, state normalization status and source links.
- Do not claim runtime validation unless a runnable starter command exists and is executed.

## Open Questions

- Should legacy docs be removed entirely, moved to an archive, or converted into generic templates?
- Should current root `AGENTS.md` remain project-specific until the new project is defined, or be generalized now?
- What minimal starter code should remain?

## Related

- [TASK.md](./TASK.md)
- [raw.md](./raw.md)
- [analysis.md](./analysis.md)
