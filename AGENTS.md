# AGENTS.md - Starter Repository Operating Contract

## 0. Precedence

When instructions or evidence conflict, apply this order:

1. Current user task, unless it requests unsafe, destructive, illegal, or explicitly forbidden behavior.
2. This root `AGENTS.md`.
3. More local `AGENTS.md` files for the touched path, such as `docs/AGENTS.md`.
4. Official artifacts under `docs/`.
5. Executable specs and tests under `tests/`.
6. Workspace notes under `llm-wiki/`.
7. Current implementation under `src/`.
8. Agent inference.

`docs/` defines intended project knowledge. `llm-wiki/` is workspace memory only and must not override official docs or current user instructions.

## 1. Baseline Guardrails

- Do not hardcode secrets, credentials, connection strings, tokens, or PII.
- Do not introduce destructive filesystem, database, or network behavior without explicit task scope.
- Keep runtime output structured and avoid logging sensitive data.
- Prefer small, reversible changes with clear verification evidence.
- Preserve user changes. Do not revert unrelated work.
- Keep implementation details in code and durable project decisions in `docs/`.

## 2. Repository Shape

This repository is currently a starter structure. The expected top-level roles are:

| Path | Role |
|---|---|
| `src/` | Application source code for the new project |
| `tests/` | Automated tests and executable examples |
| `docs/` | Official project knowledge base and Spec-First artifacts |
| `docs/grounding/` | Source registry, prompt routing, and grounding guidance |
| `docs/methodology/` | Delivery methodology and agent workflow |
| `llm-wiki/` | Agent workspace notes and active task memory |
| `specs/` | Machine-readable contracts and schemas |
| `tools/` | Local helper scripts |

Do not recreate legacy project-specific domain models, data layouts, or validation lanes unless the user explicitly defines them for the new project.

## 3. Before Editing

For tiny edits, read this file, any local `AGENTS.md`, and the touched file.

For code changes, also identify:

- entry point and caller/callee boundary;
- config or contract impact;
- smallest useful verification command;
- whether docs or tests need to change with the code.

For documentation changes under `docs/`, read `docs/AGENTS.md` and apply its KMS rules.

## 4. Documentation Rules

Official documentation must be useful for action, decision, verification, operation, onboarding, or future maintenance.

When creating or editing Markdown under `docs/`, prefer:

- YAML frontmatter with `type`, `status`, `owner`, `domain`, `project`, `created`, `updated`, `sensitivity`, `source_of_truth`, `related`, and `tags`;
- exactly one H1;
- `Purpose` or `Summary`;
- `Context` when the document is operational or architectural;
- real related links and source references.

Do not invent source-of-truth links. If evidence is missing, mark the document `draft` and record open questions.

## 5. Testing And Verification

Use the smallest verification that matches the change:

- Markdown-only cleanup: file inventory and targeted `rg` checks.
- Code change: targeted tests or executable smoke checks.
- Contract or runtime change: contract validation plus runtime smoke checks where available.
- Deletion cleanup: pre/post inventory and search evidence.

Final reports for code or documentation changes should include what changed, what was checked, and any residual risk.

## 6. Skills And Task Workspace

Use `llm-wiki/tasks/current/` for active task memory when a task needs traceability.

Task notes are not official source of truth. Promote durable knowledge into `docs/` only when the task explicitly changes official project knowledge and the evidence is clear.

## 7. Current Starter Status

This repo has been cleaned toward a new-project starter. Some methodology and KMS assets remain intentionally generic. If the new project needs domain-specific rules, add them deliberately through official docs and update grounding sources at the same time.
