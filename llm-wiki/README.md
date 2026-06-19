---
id: llm-wiki-readme
title: Hướng Dẫn llm-wiki
type: workspace_index
category: workspace_index
status: observed
owner: agent
domain: agentic-sdlc
project: ftransform
workspace: llm-wiki
created: 2026-05-13
updated: 2026-05-13
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: medium
source_of_truth:
  - docs/grounding/manifest.md
  - docs/methodology/o_2.kms_intergrate.md
  - docs/methodology/o_3.kms_obsidian_rg_runbook.md
related:
  - llm-wiki/index.md
  - llm-wiki/tasks/README.md
  - llm-wiki/source-map.md
  - llm-wiki/open-questions.md
  - llm-wiki/contradiction-log.md
tags:
  - workspace/index
  - agent/context
---

# Hướng Dẫn llm-wiki

## Mục Đích

`llm-wiki/` là workspace có kiểm soát cho agent. Thư mục này dùng để ghi nhận quan sát, phân tích, câu hỏi mở, assumption, contradiction, task memory, verification notes và lesson learned.

`llm-wiki/` không phải source of truth chính thức. Source of truth chính thức nằm trong `docs/`, executable specs, contract, ADR, QA matrix, runbook, và explicit user decision.

## Nguyên Tắc Chính

1. Dùng `llm-wiki/` để suy nghĩ và ghi nhớ trong quá trình làm task.
2. Không dùng note trong `llm-wiki/` để override `docs/`.
3. Không promote assumption chưa được xác nhận.
4. Khi phát hiện mâu thuẫn, ghi vào `contradiction-log.md`.
5. Khi thiếu thông tin để code/test/verify, ghi vào `open-questions.md`.
6. Khi một observation trở thành official knowledge, promote sang artifact phù hợp trong `docs/`.
7. Khi task có tri thức hữu ích, lưu trong `tasks/` hoặc module note liên quan.

## Cấu Trúc Chính

| Path | Vai tro |
|---|---|
| [index.md](./index.md) | Bản index hiện có của agent workspace |
| [tasks/](./tasks/README.md) | Workspace cho task hiện tại và archive task |
| [source-map.md](./source-map.md) | Map topic/task sang source chính thức |
| [codebase-map.md](./codebase-map.md) | Bản đồ quan sát về codebase |
| [open-questions.md](./open-questions.md) | Câu hỏi còn thiếu quyết định |
| [assumptions.md](./assumptions.md) | Assumption tạm thời của agent |
| [contradiction-log.md](./contradiction-log.md) | Mâu thuẫn giữa docs, tests, src và workspace |
| [lessons-learned.md](./lessons-learned.md) | Bài học có giá trị tái sử dụng |
| [agent-verification.md](./agent-verification.md) | Hướng dẫn workspace về verification evidence |
| [module-notes/](./module-notes/README.md) | Ghi chú theo module/code boundary |

## Luồng Làm Việc Với Task Mới

1. Dùng Skill 9 với mô tả task dự định làm để tạo `llm-wiki/tasks/current/`.
2. Skill 9 phân tích mục tiêu task, task type, artifact layer, verification expectation và lịch sử task liên quan.
3. Dùng grounding sources trong `docs/grounding/manifest.md`, `manifest.yaml`, `sources.md`, và `prompts.md`.
4. Ghi decision vào `decision-log.md`.
5. Nếu cần official hóa, ghi target trong `promotion.md` và cập nhật artifact tương ứng trong `docs/`.
6. Verify theo docs/tests/contract/QA/runbook.
7. Archive `current` bằng Skill 8 khi task xong và có nội dung hữu ích.
8. Task history trong `llm-wiki/tasks/YYYYMMDD_NNN_short-name/` có thể được tham khảo lại khi task mới liên quan đến quá trình xây dựng hệ thống trước đó.

## Khi Nào Promote Sang docs

Promote sang `docs/` khi:

1. Requirement đã được xác nhận.
2. Business rule hoặc business flow đã chốt.
3. ADR hoặc quyết định kiến trúc đã được chấp nhận.
4. Contract, application spec, QA matrix, runbook, prompt, skill hoặc guardrail chính thức thay đổi.
5. Verification rule trở thành quy tắc dùng lại được.

Không promote khi:

1. Chỉ là raw input.
2. Chỉ là assumption.
3. Chỉ là phân tích tạm thời.
4. Chỉ là quan sát code chưa rõ đúng/sai.
5. Chỉ là đề xuất chưa được human hoặc source chính thức chốt.

## References

- [manifest.md](../docs/grounding/manifest.md)
- [sources.md](../docs/grounding/sources.md)
- [prompts.md](../docs/grounding/prompts.md)
- [o_2.kms_intergrate.md](../docs/methodology/o_2.kms_intergrate.md)
- [o_3.kms_obsidian_rg_runbook.md](../docs/methodology/o_3.kms_obsidian_rg_runbook.md)
