# SDD Antigravity Rule

Use this rule for all Antigravity work in this repository.

## Required Grounding

Before implementation, read or retrieve enough context from:

1. `AGENTS.md`.
2. `docs/AGENTS.md` when editing `docs/`.
3. `GEMINI.md`.
4. `docs/antigravity/playbook.md`.
5. `docs/grounding/manifest.md`.
6. `docs/grounding/sources.md`.
7. `docs/grounding/prompts.md`.
8. The relevant canonical skill guide in `docs/grounding/skills/`.

## Workflow

1. Start with request intake unless the user asks only for a short explanation.
2. Build context with `.\tools\kb.ps1 context "<keyword>"` or a narrower command.
3. Use `.\tools\kb.ps1 trace "<feature-or-module>"` for behavior, interface, or documentation changes.
4. Use `.\tools\kb.ps1 sync-check "<feature-or-module>"` when docs/spec/tests/workspace/source alignment matters.
5. Use `.\tools\kb.ps1 audit-ci` for KMS, grounding, prompt, manifest, or source catalog changes.
6. Record verification evidence or blockers in `llm-wiki/tasks/current/verification-notes.md` when task traceability is needed.
7. Classify sync/drift after completion with `kms-sync-drift-control`.

## Autonomy Limits

- Do not run destructive commands unless explicitly requested.
- Do not install dependencies or tools unless explicitly requested.
- Do not modify unrelated files.
- Do not promote `llm-wiki/` observations to official docs without evidence and registry updates.
- Do not claim `VERIFIED` unless commands/artifacts were inspected.

## Handoff Output

Antigravity handoff must include:

- files changed;
- grounding sources used;
- verification run and result;
- skipped checks and reason;
- sync/drift decision;
- remaining risks.

