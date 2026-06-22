---
id: grounding-readme
title: README
type: guide
category: grounding
status: published
owner: grounding
domain: grounding
project: fcli
workspace: docs
created: '2026-04-27'
updated: 2026-05-14
sensitivity: internal
truth_level: official
source_policy: source_of_truth
source_of_truth:
- docs/grounding/manifest.yaml
- docs/grounding/manifest.md
- AGENTS.md
- docs/methodology/methodology.md
- docs/methodology/d_3.document_strategy_and_creation_order.md
related:
- docs/grounding/sources.md
- docs/grounding/prompts.md
- docs/methodology/o_3.kms_obsidian_rg_runbook.md
- docs/grounding/manifest.md
- docs/grounding/manifest.yaml
upstream: []
downstream: []
tags:
  - kms/grounding
  - kms/guide
  - sdd/reusable
  - sdd/generic
- kms/grounding
- agent/context
- kms/obsidian
- kms/rg
---
# README

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Current KMS Model

`docs/grounding/manifest.md` is the human-readable grounding entrypoint. `manifest.yaml` is the machine-readable registry. `sources.md` catalogs source IDs and usage rules. `prompts.md` routes work into documentation, implementation, review, refactoring, testing, KMS maintenance, and workspace update lanes.

`docs/` is the official KMS. `llm-wiki/` is the controlled agent workspace and must not be treated as source of truth. Use `tools/kb.ps1` and `rg` to retrieve the smallest relevant context before changing code or durable documentation.

### Mục đích

- Thiết lập “điểm neo” thống nhất cho Grounding Pack: nơi tập trung mô tả máy-đọc (manifest), quy ước prompts, nguồn tri thức, và liên kết bộ đánh giá (evals) để hỗ trợ Spec-First & Agentic Development trong Cursor/CI.

### Nội dung cần có

1) Tổng quan Grounding Pack (khái niệm, ranh giới, vai trò ở vòng đời AI)
2) Chuẩn thư mục và file: `manifest` (chỉ mục máy-đọc), `prompts` (mẫu prompt), `sources` (nguồn tri thức), `evals` (liên kết bộ đánh giá)
3) Nguyên tắc chất lượng: nguồn đáng tin cậy, trích dẫn bắt buộc, version hóa, kiểm soát thay đổi
4) Chu trình cập nhật: đề xuất → review → version → re-index → re-eval → phát hành
5) Tiêu chuẩn đo lường: faithfulness, relevancy, coverage, latency, cost（tham chiếu `docs/00-governance/00.data-governance.md`）

### Cách tạo nội dung

1) Khởi tạo manifest theo mẫu trong `manifest.md`（ban đầu có thể ở dạng bảng Markdown；bản máy-đọc `.yaml` sẽ tạo sau）
2) Tạo/thêm prompts theo quy ước trong `prompts.md`（đặt tên, metadata, version）
3) Khai báo nguồn tri thức trong `sources.md`（phạm vi, chất lượng, giấy phép/owner）
4) Liên kết bộ evals theo `evals.md`（tối thiểu tập cơ sở và ngưỡng CI）
5) Gắn các đường dẫn cố định vào tài liệu quản trị và CI（`00.governance-rules.md`）

### Chú thích (tiền đề & đầu ra)

- Tiền đề: đã có định hướng tổng quan trong `docs/00-governance/00.data-governance.md` và quy tắc trong `docs/00-governance/00.governance-rules.md`；có hoặc sẽ có đặc tả API/Schema trong `docs/04-spec-test/` hoặc `specs/`.
- Tài liệu này là cơ sở để tạo/duy trì:
  - `docs/grounding/manifest.yaml`（máy-đọc cho CI/Agent）
  - prompts version hóa dùng bởi Agent（Cursor/ChatGPT）
  - liên kết tới bộ `evals/` và ngưỡng kiểm soát trong CI

## Related

- [sources.md](./sources.md)
- [prompts.md](./prompts.md)
- [o_3.kms_obsidian_rg_runbook.md](../methodology/o_3.kms_obsidian_rg_runbook.md)

## References

- [manifest.yaml](./manifest.yaml)
- [manifest.md](./manifest.md)
- [AGENTS.md](../../AGENTS.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
- 2026-04-28: Documented the current KMS model and llm-wiki workspace boundary.
