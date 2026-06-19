---
name: create-current-task
description: Create a fresh llm-wiki/tasks/current task workspace from a user-provided task description. Use when the user starts a new agent task, asks to create the current task profile, or provides a task brief that needs structured TASK.md/raw.md/intake.md/decision/promotion/verification notes; follows docs/grounding/skills/9.create-current-task.md and archives existing current first when needed.
---

# Create Current Task

Use this skill to create `llm-wiki/tasks/current/` from a task description.

Canonical guide: `docs/grounding/skills/9.create-current-task.md`.

Required workflow:

1. Read the canonical guide first.
2. Read `docs/methodology/Task.md` for the task workspace file model.
3. Inspect `llm-wiki/tasks/current/`.
4. If `current` has meaningful content, use `archive-current-task` first so the existing task is moved under `llm-wiki/tasks/archive/` and `current` is clean.
5. Analyze the new task description for objective, task type, risk, impacted domain/runtime model, artifact layer, expected output, likely sources, open questions, and verification expectation.
6. Search archived task history under `llm-wiki/tasks/archive/` only when the new task appears related to previous work; otherwise record that history search was skipped.
7. Create task-specific files in `llm-wiki/tasks/current/`. Always create `TASK.md`, `raw.md`, and `intake.md`; add `decision-log.md`, `promotion.md`, `verification-notes.md`, `analysis.md`, `clarification-log.md`, or `contradictions.md` when useful.
8. Do not promote workspace notes into `docs/` during task creation.

Minimum `TASK.md` content:

```text
Request summary:
Task objective:
Task type:
Impacted domain/runtime model:
Artifact layer:
Expected output:
Files in scope:
Files out of scope:
Forbidden actions:
Verification expectation:
Related archived tasks:
Open questions:
Ready status:
Next skill:
```

Output:

```text
Create Current Task Report
Task objective:
Task type:
Impacted domain/runtime model:
Artifact layer:
Files created:
Related archived tasks checked:
Open questions:
Next skill:
Final status:
```
