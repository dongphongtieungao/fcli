---
type: runbook
category: operation
status: published
owner: unknown
domain: agentic-sdlc
project: ftransform
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
  - docs/grounding/prompts.md
related:
  - ../grounding/manifest.md
  - ../grounding/sources.md
  - ../grounding/prompts.md
  - ../methodology/methodology.md
  - ../../GEMINI.md
  - ../../.agent/rules/sdd-antigravity.md
tags:
  - risk/operation
  - sdd/runbook
  - sdd/reusable
  - sdd/generic
  - agent/context
---

# Antigravity Playbook

## Purpose

Describe how to use Antigravity as an adapter for the reusable SDD/KMS model in this starter repository.

## Context

This playbook is tool-facing guidance. It does not replace `AGENTS.md` or official docs. Antigravity must treat `docs/grounding/skills/` as the canonical SDD skill pack and `.agent/skills/` as adapter skills only.

## Minimum Reading Set

1. `AGENTS.md`
2. `docs/AGENTS.md` when editing docs
3. `docs/grounding/manifest.md`
4. `docs/grounding/sources.md`
5. `docs/grounding/prompts.md`
6. `GEMINI.md`
7. `.agent/rules/sdd-antigravity.md`
8. Relevant task workspace notes under `llm-wiki/tasks/current/`

## Antigravity Adapter Model

| Layer | Repository source | Antigravity use |
|---|---|---|
| Policy | `AGENTS.md`, `docs/AGENTS.md` | Mandatory operating rules and docs KMS rules |
| Bridge | `GEMINI.md` | Thin Antigravity/Gemini-facing startup guidance |
| Rule | `.agent/rules/sdd-antigravity.md` | Grounding, retrieval, verification, and sync behavior |
| Adapter skills | `.agent/skills/*/SKILL.md` | Antigravity-readable wrappers |
| Canonical skills | `docs/grounding/skills/*.md` | Source of truth for SDD skill behavior |
| Retrieval | `tools/kb.ps1`, `.ripgreprc` | Context, trace, sync, and audit commands |
| Workspace | `llm-wiki/tasks/current/` | Task evidence and temporary memory |

## Workflow

1. Intake the task with `sdd-request-intake` unless it is a tiny explanation.
2. Retrieve grounded context with `kms-retrieval-context-pack` and `tools/kb.ps1`.
3. Route the lane with `prompt-lane-router` when work needs planning, implementation, debugging, review, documentation, KMS maintenance, or workspace update.
4. Map acceptance with `spec-acceptance-mapping` when behavior, contracts, workflows, or generated output are affected.
5. Analyze code boundaries with `code-boundary-file-role` before code edits.
6. Implement in scope with editor and terminal actions constrained by `AGENTS.md`.
7. Verify with `evidence-verification`; inspect warnings, errors, and artifacts before claiming completion.
8. Sync or record drift with `kms-sync-drift-control`.
9. Archive or create task workspaces only through the archive/create task skills.

## Editor, Terminal, Browser, And Artifact Use

| Surface | Rule |
|---|---|
| Editor | Keep diffs scoped. Do not modify unrelated files or rewrite canonical docs when an adapter link is enough. |
| Terminal | Prefer `tools/kb.ps1` commands and smallest validation first. Do not install dependencies without explicit request. |
| Browser | Use browser checks only when a local app, UI, rendered doc, or web artifact has a clear target. |
| Artifacts | Treat generated plans, screenshots, logs, and browser output as evidence candidates, not source of truth. |
| Workspace | Record task evidence in `llm-wiki/tasks/current/` when traceability matters. |

## SDD Phase Mapping

| SDD phase | Antigravity action | Expected evidence |
|---|---|---|
| Intake | Produce task brief | Risk, scope, source, readiness, next skill |
| Grounding | Run `.\tools\kb.ps1 context "<keyword>"` or narrower search | Files and commands used |
| Planning | Select prompt lane and implementation boundary | Lane, prompt contract, files in/out |
| Implementation | Edit only scoped files | Minimal diff |
| Verification | Run targeted checks, inspect artifacts | Evidence pack and final status |
| Sync | Update workspace/docs/registry only when justified | Sync report or no-sync decision |

## Reuse Rules

| Rule | Description |
|---|---|
| Start with task profile | Classify analysis, behavior change, implementation, docs, verification, or release work. |
| Prefer official docs | Use workspace notes for context, not as source-of-truth. |
| Keep artifacts generic | Do not reintroduce old project assumptions into starter assets. |
| Verify claims | Completion must be backed by evidence or reported as partial/blocked. |
| Promote deliberately | Durable findings move to the correct docs layer with trace links. |

## Autonomy Guardrails

- Do not run destructive commands unless the user explicitly requests them.
- Do not install tools, dependencies, extensions, or connectors unless explicitly requested.
- Do not change dependency files or generated lock files unless the task requires it.
- Do not create new prompt lanes, source IDs, or durable rules without updating `manifest.yaml`, `sources.md`, and `prompts.md` when relevant.
- Do not treat Antigravity memory, task artifacts, or `llm-wiki/` notes as official source of truth.
- Do not claim `VERIFIED` unless the exact evidence has been inspected.

## Handoff Checklist

- [ ] Scope and files changed are clear.
- [ ] Source-of-truth files used are named.
- [ ] Verification evidence or blocker is recorded.
- [ ] Promotion/sync status is known.
- [ ] Residual risks are visible to the next agent or developer.
- [ ] Antigravity adapter skill used is named when relevant.
- [ ] Registry impact is handled when adapter docs, rules, skills, prompts, or sources change.

## Related

- [Grounding Manifest](../grounding/manifest.md)
- [Grounding Sources](../grounding/sources.md)
- [Prompt Governance](../grounding/prompts.md)
- [Methodology](../methodology/methodology.md)
- [Antigravity Bridge](../../GEMINI.md)
- [SDD Antigravity Rule](../../.agent/rules/sdd-antigravity.md)
