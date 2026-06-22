---
id: llm-wiki-tasks-readme
title: Task Workspace README
type: workspace_index
category: workspace_index
status: observed
owner: agent
domain: agentic-sdlc
project: fcli
workspace: llm-wiki
created: 2026-05-13
updated: 2026-05-13
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: medium
source_of_truth:
  - docs/methodology/Task.md
  - docs/grounding/manifest.md
  - docs/grounding/skills/8.archive-current-task.md
  - docs/grounding/skills/9.create-current-task.md
related:
  - llm-wiki/index.md
  - llm-wiki/tasks/current/TASK.md
  - docs/methodology/Task.md
  - docs/grounding/skills/9.create-current-task.md
tags:
  - workspace/task
  - workspace/index
---

# Task Workspace README

## Mục Đích

Thư mục này lưu workspace cho task hiện tại và lịch sử task đã archive của agent.

Đây là vùng làm việc tạm thời trong `llm-wiki/`, không phải source of truth chính thức. Quy trình tạo task, cấu trúc file, archive và truy vết task history được định nghĩa trong [Task.md](../../docs/methodology/Task.md).

## Quy Ước Thư Mục

```text
llm-wiki/tasks/current/
llm-wiki/tasks/archive/YYYYMMDD_NNN_short-name/
```

`current` là workspace của task đang làm. Các thư mục có ngày trong `archive/` là lịch sử task đã archive. `archive/` là tư liệu để search khi cần truy vết, không phải thư mục agent cần scan mặc định cho mọi task.

## Cách Dùng

1. Khi nhận task mới, dùng Skill 9 với mô tả task dự định làm.
2. Skill 9 dùng [Task.md](../../docs/methodology/Task.md) để tạo nội dung phù hợp trong `current/`.
3. Sau khi task xong, nếu `current` có nội dung hữu ích, dùng Skill 8 để archive sang `archive/YYYYMMDD_NNN_short-name` rồi clean `current`.
4. Task tiếp theo lại bắt đầu bằng Skill 9.

## Skill Liên Quan

- [8.archive-current-task.md](../../docs/grounding/skills/8.archive-current-task.md): archive `current` sang `llm-wiki/tasks/archive/` và clean `current`.
- [9.create-current-task.md](../../docs/grounding/skills/9.create-current-task.md): tạo `current` từ mô tả task mới và lịch sử task liên quan.

## References

- [Task.md](../../docs/methodology/Task.md)
- [manifest.md](../../docs/grounding/manifest.md)
- [8.archive-current-task.md](../../docs/grounding/skills/8.archive-current-task.md)
- [9.create-current-task.md](../../docs/grounding/skills/9.create-current-task.md)

## Change Notes

- 2026-05-13: Created task workspace README.
- 2026-05-13: Moved task creation guidance into `docs/methodology/Task.md`.
- 2026-05-13: Updated archive convention to use `llm-wiki/tasks/archive/`.
