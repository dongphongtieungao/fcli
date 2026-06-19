# Antigravity Agent Bridge

This file is the Antigravity/Gemini-facing bridge for this repository. It does not replace `AGENTS.md`.

## Operating Order

1. Read `AGENTS.md` before code or documentation changes.
2. Read `docs/AGENTS.md` before creating, editing, moving, promoting, or deleting files under `docs/`.
3. Read `docs/antigravity/playbook.md` for Antigravity-specific workflow.
4. Use `docs/grounding/manifest.md`, `docs/grounding/manifest.yaml`, `docs/grounding/sources.md`, and `docs/grounding/prompts.md` as the SDD/KMS grounding pack.
5. Use `tools/kb.ps1` and `.ripgreprc` for retrieval before broad file reads.
6. Use `llm-wiki/tasks/current/` for task evidence when traceability is needed.

## Antigravity Rules

- Treat `docs/` as official KMS and `llm-wiki/` as workspace-only memory.
- Do not treat Antigravity memory, generated plans, or browser artifacts as source of truth unless promoted into `docs/` with evidence.
- Keep SDD core canonical in `docs/grounding/skills/`; `.agent/skills/` contains Antigravity adapters only.
- Do not create a new lane, prompt, source id, or durable rule without updating the grounding registry.
- Do not claim completion without verification evidence or a clearly reported blocker.

## Retrieval Defaults

Prefer these commands when relevant:

```powershell
.\tools\kb.ps1 status
.\tools\kb.ps1 grounding
.\tools\kb.ps1 context "<keyword>"
.\tools\kb.ps1 trace "<feature-or-module>"
.\tools\kb.ps1 sync-check "<feature-or-module>"
.\tools\kb.ps1 audit-ci
```

## Skill Pack

Use the 9 Antigravity SDD adapter skills under `.agent/skills/`. Each adapter points back to the canonical guide in `docs/grounding/skills/`.

