---
type: technical_note
status: draft
owner: unknown
domain: workspace
project: ftransform
created: 2026-05-13
updated: 2026-05-13
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/grounding/skills/9.create-current-task.md
  - docs/grounding/skills/8.archive-current-task.md
  - docs/methodology/Task.md
related:
  - raw.md
  - intake.md
  - analysis.md
  - deletion-audit.md
  - decision-log.md
  - promotion.md
  - verification-notes.md
tags:
  - workspace/task
  - project-reset
---

# Current Task

## Purpose

Track the active task to clean remaining legacy domain-specific code and documentation so this repository can be reused as a starting structure for a new project.

## Task Brief

Request summary: User wants to initialize a new project by reviewing and cleaning code and documentation that still relates to the old project after most old material has already been removed.

Task objective: Identify and remove or quarantine legacy project-specific content while preserving reusable project structure and repository operating rules.

Task type: repository cleanup, documentation cleanup, project reset.

Impacted module: shared / n/a. This task is about removing legacy project coupling rather than changing a specific runtime module.

Artifact layer: workspace-only intake for now; likely documentation/KMS and source cleanup after analysis.

Expected output: A cleaned starter repository with old-project-specific code/docs removed or clearly isolated, plus verification notes describing what changed and what remains.

Files in scope: Repository files that reference the old project, including legacy `src/`, `docs/`, `llm-wiki/`, `verify/`, `tools/`, config/examples, and metadata where appropriate.

Files out of scope: Secrets, external dependencies, archived task history, and unrelated user changes unless explicitly confirmed.

Forbidden actions: Do not introduce DML/DDL, secrets, hardcoded credentials, or ungrounded business rules. Do not delete archived task history. Do not remove reusable structure blindly.

Verification expectation: Inventory old-project references first; then verify cleanup with `rg` scans and any lightweight structural checks that fit the remaining repo.

Related archived tasks: Archive history search skipped for task creation because no archive entries are currently present.

Open questions:

- What exact starter structure should remain after cleanup?
- Should old project documents be deleted, archived, or rewritten as generic templates?
- Should legacy code modules be removed entirely or replaced with minimal placeholders?

Ready status: Ready for repository inventory and cleanup planning; not yet ready for deletion without an inventory.

Next skill: `kms-retrieval-context-pack` or direct repository inventory, then cleanup implementation.

## Related

- [raw.md](./raw.md)
- [intake.md](./intake.md)
- [analysis.md](./analysis.md)
- [deletion-audit.md](./deletion-audit.md)
- [decision-log.md](./decision-log.md)
- [promotion.md](./promotion.md)
- [verification-notes.md](./verification-notes.md)
