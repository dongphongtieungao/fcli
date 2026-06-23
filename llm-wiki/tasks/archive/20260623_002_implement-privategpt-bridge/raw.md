---
id: task-implement-privategpt-bridge-raw
title: Raw Implementation Request
type: task_memory
category: agent_workspace
status: observed
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
truth_level: observed
source_policy: workspace_only
confidence: high
source_of_truth:
  - Current user instruction, 2026-06-23
related:
  - llm-wiki/tasks/current/TASK.md
  - llm-wiki/tasks/current/intake.md
verified_against: []
upstream: []
downstream:
  - llm-wiki/tasks/current/intake.md
tags:
  - workspace/task
  - agent/context
---

# Raw Implementation Request

## Purpose

Preserve the user's implementation request before analysis.

## Original Request

```text
[$create-current-task](C:\Sk\opencode-fcli\.agents\skills\create-current-task\SKILL.md) thực hiện implement code theo nghiệp vụ và thiết kế.
```

## Interpretation Boundary

The request authorizes implementation grounded in existing business and design artifacts. It does not authorize new business behavior, changes to external systems, deployment, or publication of unverified workspace observations.

## Related

- [Current Task](./TASK.md)
- [Task Intake](./intake.md)
