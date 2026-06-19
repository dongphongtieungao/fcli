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

- Chuẩn hóa định dạng test case cho evals, đảm bảo có thể máy-đọc, có trích dẫn nguồn (citations) khớp với manifest, và phản ánh đúng nghiệp vụ.

### Vai trò

- Cầu nối giữa Grounding Pack và chấm điểm tự động; giúp CI xác định lỗi do thiếu nguồn, trích dẫn kém hoặc sai ngữ cảnh.

### Định dạng file

- Khuyến nghị `JSONL` (mỗi dòng một case) hoặc `JSON` danh sách case.
- Cấu trúc một case:
  {
    "id": "<duy_nhat>",
    "query": "<cau_hoi/tac_vu>",
    "expected_citations": [
      { "source_id": "<id_trong_manifest>", "min_segments": 1 }
    ],
    "gold_answer": "<tuy_chon>",
    "domain": "<module/domain>",
    "notes": "<chu_thich_tu_nguyen>"
  }

### Cách tạo nội dung

1) Golden: chọn các kịch bản thường gặp, câu trả lời có căn cứ rõ ràng, citations trỏ đúng `source_id` trong manifest.
2) Adversarial: bổ sung kịch bản lắt léo (tiêm nhiễm prompt, mơ hồ, đối tượng/điều kiện biên) để kiểm độ bền.
3) Regression: khi sửa lỗi, chụp lại case gây lỗi thành regression để ngăn tái phát.

### Chú thích

- Tiền đề: đã cập nhật `docs/grounding/manifest.md|yaml` để tham chiếu `source_id`.
- Đầu ra: các file `*.jsonl` trong thư mục này được `evals/config.yaml` gọi trong CI để chấm điểm.

## Related




## References

- [manifest.yaml](../../../docs/grounding/manifest.yaml)
- [manifest.md](../../../docs/grounding/manifest.md)
- [AGENTS.md](../../../AGENTS.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
