---
name: evidence-verification
description: Verify agent work with evidence instead of claims. Use after code, tests, official docs, prompts, KMS artifacts, runtime behavior, report generation, PPTX output, application output, or any agent completion claim to resolve canonical commands, run or record checks, inspect warnings/errors/artifacts, and assign VERIFIED, PARTIALLY VERIFIED, FAILED, BLOCKED, or NEEDS HUMAN REVIEW.
---

# Evidence Verification

Use this skill before claiming work is done.

Canonical guide: `docs/grounding/skills/6.evidence-verification.md`.

Procedure:

1. Read the canonical guide.
2. Resolve canonical verification sources before running commands: application spec, QA matrix, DoD, runbook, CI workflow, existing tests, or task-specific instruction.
3. For report generation, include `--debug` unless a canonical source explicitly omits it.
4. Record command source, exact command, exit code, warnings, errors, artifacts, and skipped checks.
5. For generated outputs, record output path and useful metadata such as size, count, checksum, page count, or response code when available.
6. Do not use `VERIFIED` when warnings/errors are unread or artifact inspection is incomplete.

Output:

```text
Evidence Pack
Task summary:
Task type:
Affected files:
Affected module:
Grounding sources:
Verification performed:
Command source:
Command:
Exit code:
Warnings:
Errors:
Artifacts:
PPTX output path:
Artifact metadata:
Manual inspection:
Skipped or not run:
Reason for skipped verification:
Remaining risks:
Final status:
```
