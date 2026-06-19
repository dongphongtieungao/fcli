---
name: plan-task
description: Create a concise implementation or review plan for a repo task by orchestrating the SDD Codex skills. Use when the user asks to plan, analyze before doing, break down work, identify modules/files to read or change, define verification, or decide which SDD skills should run before implementation.
---

# Plan Task

Use this as the thin planning orchestrator for repo work. Do not duplicate the detailed SDD procedures here; call the specialized skills when their trigger applies.

Skill order:

1. Use `sdd-request-intake` for task type, risk, impacted domain/runtime model, scope, forbidden actions, and readiness.
2. Use `kms-retrieval-context-pack` when context must be gathered from policy, grounding, docs, tests, `llm-wiki`, or source.
3. Use `prompt-lane-router` when a lane or prompt contract is needed.
4. Use `spec-acceptance-mapping` when behavior, generated output, application, data contract, runtime flow, or business rules may change.
5. Use `code-boundary-file-role` before touching important source files or model-specific report code.
6. Use `evidence-verification` to define the verification plan and later verify the result.
7. Use `kms-sync-drift-control` when durable observations, official docs, prompts, skills, manifest, sources, or drift may need sync.

Planning rules:

1. Read `AGENTS.md` first for non-trivial tasks.
2. If the task touches `docs/`, read `docs/AGENTS.md`.
3. If the task touches a subtree with local policy, identify the impacted module and read the local `AGENTS.md`.
4. Keep the plan scoped; list files out of scope as well as in scope.
5. Do not propose implementation until the readiness gate and verification method are clear.

Output:

```text
Plan Task
Task summary:
Task type:
Risk level:
Impacted domain/runtime model:
Required skills:
Grounding sources to read:
Files to inspect:
Files likely to change:
Files out of scope:
Implementation steps:
Verification plan:
KMS sync plan:
Blockers or open questions:
Ready status:
```
