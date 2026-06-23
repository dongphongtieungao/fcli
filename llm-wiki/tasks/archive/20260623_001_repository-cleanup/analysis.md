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
  - intake.md
  - verification-notes.md
tags:
  - workspace/task
  - analysis
---

# Cleanup Analysis

## Purpose

Capture inventory findings and cleanup reasoning during the project reset.

## Context

Initial task creation has not yet performed repository cleanup. This file is ready to record findings from scans and decisions about what is reusable structure versus legacy project content.

## Initial Hypothesis

The repository likely still contains official docs and agent policy that reference the old project. Because those files define source-of-truth and operating rules, they should be inventoried before any broad deletion or rewrite.

## Planned Inventory

- File inventory: `rg --files`
- Legacy keywords: old domain names, old mode names, old output artifact names, and obsolete validation tooling.
- Project-specific paths: legacy service, verification, generated-data, and tool paths.

## Inventory Findings

- Source/runtime directories `src/`, `tests/`, and `verify/` are currently empty.
- Legacy coupling was concentrated in official docs, grounding prompt lanes, root agent policy, workspace notes, schemas, and generated diagnostic artifacts.
- Reusable assets retained: docs policy, grounding/methodology skills, task workspace workflow, metadata/taxonomy, sample eval structure, helper tools, and starter schema examples.
- Removed or rewritten old project entrypoints so the repo no longer presents the previous application domain as the active source of truth.
- Over-deletion audit restored reusable generic governance, template, QA, review, runbook, and grounding template content while keeping old domain artifacts deleted.

## Commands Used

```powershell
rg --files
rg -n --heading --smart-case "<legacy-domain-keyword-regex>" docs llm-wiki specs -g "*.md" -g "*.yaml" -g "*.json"
rg -n --heading --smart-case "<legacy-domain-keyword-regex>" -g "!*.png" -g "!*.jpg" -g "!*.jpeg" -g "!*.zip" -g "!.git/**" -g "!llm-wiki/tasks/current/**"
git status --short
```

## Related

- [TASK.md](./TASK.md)
- [intake.md](./intake.md)
- [verification-notes.md](./verification-notes.md)
