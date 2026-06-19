---
name: kms-retrieval-context-pack
description: Build a repeatable KMS context pack using rg or tools/kb.ps1 before planning, coding, review, verification, or KMS updates. Use after intake when sources, specs, tests, llm-wiki notes, source locations, blockers, contradictions, impacted domain/runtime model, or context confidence must be gathered and recorded.
---

# KMS Retrieval Context Pack

Use this skill after `sdd-request-intake` when a task needs grounded context.

Canonical guide: `docs/grounding/skills/2.kms-retrieval-context-pack.md`.

Procedure:

1. Read the canonical guide.
2. Use `rg` or `.\tools\kb.ps1` to search in this order: policy/grounding, official docs, executable specs, workspace notes, source code.
3. Record the exact commands or searches used.
4. Keep context small; summarize only relevant sources.
5. Mark `llm-wiki/` as workspace-only and `src/` as implementation-current.

Useful commands:

```powershell
.\tools\kb.ps1 status
.\tools\kb.ps1 grounding
.\tools\kb.ps1 docs "<keyword>"
.\tools\kb.ps1 specs "<keyword>"
.\tools\kb.ps1 workspace "<keyword>"
.\tools\kb.ps1 code "<keyword>"
.\tools\kb.ps1 blockers
.\tools\kb.ps1 contradictions
```

Output:

```text
Context Pack
Request keyword:
Task type:
Risk level:
Commands or searches used:
Official docs found:
Tests or executable specs found:
Workspace notes found:
Source code locations found:
Blockers or contradictions:
Missing sources:
Impacted domain/runtime model:
Local policy needed:
Context confidence:
Recommended next skill:
```
