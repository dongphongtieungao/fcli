---
name: prompt-lane-router
description: Select the correct prompt lane and prompt contract from repo prompt governance. Use after intake and context retrieval when work must be routed to analysis, planning, implementation, review, refactoring, testing, documentation, KMS maintenance, workspace update, or release handover without inventing new lanes or ignoring required sources.
---

# Prompt Lane Router

Use this skill after a Task Brief and Context Pack exist.

Canonical guide: `docs/grounding/skills/3.prompt-lane-router.md`.

Procedure:

1. Read `docs/grounding/prompts.md`.
2. Match the task to an existing lane.
3. Resolve prompt files and source IDs through `docs/grounding/manifest.yaml` or `docs/grounding/sources.md`.
4. Do not create a new prompt lane during routing.
5. If a needed lane or prompt does not exist, report the gap and route to KMS maintenance or human review.

Output:

```text
Prompt Routing Result
Selected primary lane:
Secondary lanes:
Selected prompt files:
Unresolved prompt files:
Required sources:
Resolved source paths:
Execution role:
Scope:
Files in scope:
Files out of scope:
Forbidden actions:
Output contract:
Verification expectation:
Prompt readiness:
Next skill:
```
