---
type: guide
status: published
owner: unknown
domain: unknown
project: unknown
created: 2026-04-27
updated: 2026-04-27
sensitivity: internal
source_of_truth:
  - docs/grounding/manifest.yaml
  - docs/grounding/manifest.md
  - AGENTS.md
related:
  
  
tags:
  - normalized
---
# README

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

### Mục đích

- Thiết lập hệ thống đánh giá (evals) làm guardrails cho Agent/AI: xác minh tính đúng đắn (faithfulness), mức liên quan (relevancy), độ bao phủ ngữ cảnh (context precision/recall), độ trễ (latency) và chi phí (cost) theo chuẩn Spec-First & Agentic Development.

### Vai trò

- Là “cổng kiểm” trong CI: PR có thay đổi liên quan prompts/grounding/sources/tool I/O/spec phải qua evals đạt ngưỡng mới được hợp nhất.
- Cung cấp bộ dữ liệu chuẩn (golden), đối kháng (adversarial) và hồi quy (regression) để giám sát chất lượng theo thời gian.

### Cấu trúc thư mục

- `evals/config.yaml`: ngưỡng, chỉ số, trọng số, chế độ chạy.
- `evals/cases/`: chứa bộ test case (JSONL/JSON) theo từng loại (golden/adversarial/regression).
- `evals/cases/README.md`: hướng dẫn format case và quy tắc trích dẫn.

### Cách tạo nội dung

1) Xác định chỉ số & ngưỡng tối thiểu trong `config.yaml` (ví dụ: faithfulness ≥ 0.75, answer_relevancy ≥ 0.7).
2) Tạo case nền tảng (golden) phản ánh đúng tác vụ, có `query` và yêu cầu `expected_citations` (ít nhất N nguồn từ `docs/grounding/manifest.md|yaml`).
3) Bổ sung adversarial khi phát hiện lỗi/tiêm nhiễm prompt/ngoại lệ nghiệp vụ; thêm regression khi sửa lỗi để ngăn tái phát.
4) Kết nối CI để tự động chạy evals khi thay đổi prompts/manifest/sources/spec/tool I/O.

### Chú thích

- Tiền đề: đã có prompts và Grounding Pack (`docs/grounding/manifest.md|yaml`, `sources.md`), và quy tắc trong `docs/00-governance/00.governance-rules.md`.
- Tài liệu/thư mục này là cơ sở để: (1) thiết lập ngưỡng guardrails CI; (2) phản hồi cải tiến prompts/grounding; (3) minh chứng chất lượng qua thời gian.

## Related




## References

- [manifest.yaml](../../docs/grounding/manifest.yaml)
- [manifest.md](../../docs/grounding/manifest.md)
- [AGENTS.md](../../AGENTS.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
