---
name: archive-current-task
description: Archive the current agent task workspace. Use when the user asks to archive, close, finish, clean, or preserve llm-wiki/tasks/current, or before starting a new task when current has meaningful content; follows docs/grounding/skills/8.archive-current-task.md by moving content to llm-wiki/tasks/archive/YYYYMMDD_NNN_short-name and leaving current clean.
---

# Archive Current Task

Use this skill to archive the current `llm-wiki/tasks/current/` workspace.

Canonical guide: `docs/grounding/skills/8.archive-current-task.md`.

Required workflow:

1. Read the canonical guide first.
2. Read `docs/methodology/Task.md` for task workspace lifecycle rules.
3. Check whether `llm-wiki/tasks/current/` exists and contains meaningful content.
4. If there is no meaningful current task content, report that no archive was needed and ensure `llm-wiki/tasks/current/` exists as a clean directory.
5. If meaningful content exists, select a non-colliding archive folder under `llm-wiki/tasks/archive/YYYYMMDD_NNN_short-name/`.
6. Move the current task files into the archive folder without overwriting existing task history.
7. Clean `llm-wiki/tasks/current/` by removing remaining files and subdirectories, then recreate the empty directory.
8. If a next task description is available, use `create-current-task` after archiving to populate `current` from that description.
9. Do not promote workspace notes into `docs/` unless the user explicitly asks.

Safety rules:

1. Do not delete archived task folders.
2. Do not overwrite an existing archive folder.
3. Do not copy blank template files into `current`.
4. Do not scan all archived task history unless the current task needs history lookup.
5. Do not use destructive shell pipelines; perform path checks before moving or deleting files.

Output:

```text
Archive Current Task Report
Current task had content:
Archive folder:
Sequence selected:
Files archived:
Fresh current directory:
Next task skill:
Docs promotion performed:
Manifest or sources update needed:
Final status:
```
