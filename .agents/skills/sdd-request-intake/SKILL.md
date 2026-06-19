---
name: sdd-request-intake
description: Convert unclear or non-trivial user requests into an SDD task brief before planning or implementation. Use for code changes, reviews, documentation/KMS updates, behavior changes, validation requests, or any request missing scope, risk, source of truth, impacted domain/runtime model, files in/out of scope, forbidden actions, or verification expectations.
---

# SDD Request Intake

Use this skill before non-trivial work in this repo.

Canonical guide: `docs/grounding/skills/1.sdd-request-intake.md`.

Procedure:

1. Read the current user request.
2. Read `AGENTS.md`; if the request touches `docs/`, also read `docs/AGENTS.md`.
3. If the request touches module-specific code, identify the module, `shared`, or `n/a`, and read the relevant local `AGENTS.md` when present.
4. Produce the Task Brief from the canonical guide.
5. Do not move to implementation unless readiness is explicitly `READY_FOR_IMPLEMENTATION`.

Output:

```text
Task Brief
Request summary:
Task type:
Secondary task types:
Risk level:
Impacted domain/runtime model:
Local AGENTS policy:
Expected output:
Grounding sources to read:
Files in scope:
Files out of scope:
Forbidden actions:
Open questions:
Verification expectation:
Ready status:
Next skill:
```
