---
name: kms-sync-drift-control
description: Decide whether task results require updates to llm-wiki, official docs, grounding manifest, sources catalog, prompts registry, taxonomy, or no KMS sync. Use after verification when durable observations, assumptions, open questions, contradictions, prompt/skill changes, official docs changes, or source-of-truth drift may exist.
---

# KMS Sync Drift Control

Use this skill after verification to prevent knowledge drift.

Canonical guide: `docs/grounding/skills/7.kms-sync-drift-control.md`.

Procedure:

1. Read the canonical guide.
2. Classify the task outcome: no durable knowledge, workspace observation, assumption, open question, contradiction, official docs change, prompt/skill change, registry change, or verification-only.
3. Keep `llm-wiki/` workspace-only unless evidence or a decision promotes content into `docs/`.
4. If official artifacts change, decide whether `manifest.md`, `manifest.yaml`, `sources.md`, or `prompts.md` must be updated.
5. Use the minimum necessary update; do not rewrite large docs outside scope.

Useful commands:

```powershell
.\tools\kb.ps1 audit
.\tools\kb.ps1 audit-metadata
.\tools\kb.ps1 missing-links
.\tools\kb.ps1 contradictions
```

Output:

```text
KMS Sync Report
Task outcome type:
Sync needed:
Sync targets:
Official docs changed:
Workspace notes changed:
Manifest update needed:
Sources update needed:
Prompts registry update needed:
Taxonomy or metadata update needed:
Contradictions recorded:
Open questions recorded:
Assumptions recorded:
Lessons learned recorded:
Commands or checks:
Remaining drift risk:
Final KMS status:
```
