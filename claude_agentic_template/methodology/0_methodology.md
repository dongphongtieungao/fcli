---
type: methodology
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
  - docs/methodology/methodology.md
  - docs/methodology/d_3.document_strategy_and_creation_order.md
related:
  
  
  - docs/MOC_Methodology.md
  - docs/methodology/d_3.document_strategy_and_creation_order.md
  - docs/methodology/d_1.quickstarts.md
tags:
  - normalized
---
# HƯỚNG DẪN TRIỂN KHAI NHANH: AGENTIC SOFTWARE ENGINEERING + SPEC-FIRST GenAI SDLC (Python)

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `claude_agentic_template/methodology/0_methodology.md` and connects it to the repository documentation graph for Agentic Spec First work.

> Tham khảo và thực hiện để tăng tốc phát triển phần mềm hiện đại (Python, AI, Cursor IDE, ChatGPT Plus)

---

## 1. Tư duy tổng quan

- **Spec-First & Agentic Development:** Mọi code, test, tài liệu đều lấy đặc tả làm "ground truth" (OpenAPI/Schema, ADR, BPMN, coding standard).
- **AI-Driven Scaffold & Dev:** Sử dụng AI (Cursor IDE, ChatGPT, v.v.) để sinh mã, test, seed dữ liệu, tài liệu vận hành; human focus vào verify, kiến trúc hóa, review.
- **Agentic Workflow:** Chia vai AI làm planner, coder, tester, reviewer, lặp Plan → Code → Test → Run.

---

## 2. Lộ trình triển khai thực tiễn (7 ngày khởi động chuẩn hóa)

### **Ngày 1–2: Chuẩn hóa kiến trúc & guardrails**
  - [ ] Vẽ/Cập nhật sơ đồ kiến trúc hệ thống (C4, có thể dùng diagram PNG/PlantUML).
  - [ ] Ghi nhận ADR (kiểu [mẫu này](https://github.com/npryce/adr-tools/blob/master/doc/adr-template.md)).
  - [ ] Viết chuẩn code (`docs/02-architecture/08.standards.md`, chọn PEP8, naming rule, convention team).
  - [ ] Tạo `docs/06-quality-assurance/13.prompt-guardrails.md`: quy tắc kiểm soát AI/codegen (sample dưới).

### **Ngày 3: Đặc tả & contract-first hóa**
  - [ ] Khai báo OpenAPI/JSON-Schema đặc tả nghiệp vụ chính (`docs/04-spec-test/01-10.openapi.yaml`).
  - [ ] Tạo `docs/02-architecture/02.glossary.md` (từ điển nghiệp vụ, bounded context DDD).
  - [ ] Định nghĩa test-case chính theo Gherkin hoặc bảng test (bắt đầu với 1–2 feature core).

### **Ngày 4–5: Gen code skeleton & test với AI**
  - [ ] Yêu cầu ChatGPT tạo code scaffold/module (FastAPI/Flask/Django) dựa trên 10.openapi.yaml/coding-standard.
  - [ ] Sinh test/unit test cho từng scenario trong test-cases.md.
  - [ ] Nếu dùng DB, sinh migration/schema, seed fixture mẫu.

### **Ngày 6–7: Thiết lập Dev Loop, CI & quy trình kiểm thử**
  - [ ] Tạo workflow CI (Github Actions, lint, pytest, contract test).
  - [ ] Bổ sung các module hỗ trợ (logging, error, common libs) nếu cần – có thể dùng AI gen từng khối nhỏ.
  - [ ] Hướng dẫn sinh auto-doc, runbook dựa trên đặc tả và test-case.

---

## 3. Checklist tệp nền tảng cần tạo

- `docs/C4_Context.png` hoặc `docs/C4_Context.puml` — sơ đồ kiến trúc hệ thống
- `docs/02-architecture/06.adr.md` — decision record (lập mẫu)
- `docs/02-architecture/08.standards.md` — chuẩn code
- `docs/06-quality-assurance/13.prompt-guardrails.md` — định nghĩa rule AI/codegen
- `docs/04-spec-test/01-10.openapi.yaml` — mô tả API/contract
- `docs/02-architecture/02.glossary.md` — từ điển nghiệp vụ
- `docs/test-cases.md` — kịch bản/bảng test đầu tiên (dùng Gherkin, table, checklist)
- `docs/prompt.md` — prompt nền tảng sử dụng cho AI (copy-paste vào ChatGPT/Cursor)
- `src/` — folder code Python
- `tests/` — folder code test
- `.github/workflows/` — workflow CI (yaml)

---

## 4. **Guardrails & Prompt mẫu cho AI (copy-paste dùng ngay)**

### guardrails.md (mẫu)
```
- Code/tài liệu/test mới phải bám đặc tả docs/04-spec-test/01-10.openapi.yaml, ADR (docs/02-architecture/06.adr.md), coding-standard (docs/02-architecture/08.standards.md).
- Không thêm nghiệp vụ bên ngoài hoặc tự chế unless được viết rõ trong đặc tả.
- Tất cả PR phải pass lint, unit test, contract test CI.
- Mỗi module nghiệp vụ đều traceable từ test-case và contract.
```

### prompt.md (template)
```
Bạn là agent developer. Mọi yêu cầu code/test đều phải tuân thủ docs/04-spec-test/01-10.openapi.yaml, docs/02-architecture/08.standards.md, docs/06-quality-assurance/13.prompt-guardrails.md. Nếu không rõ behavior/contract thì hỏi lại hoặc raise NotImplementedError. Không tự thêm nghiệp vụ ngoài đặc tả.

Nhiệm vụ: [ví dụ: Tạo skeleton FastAPI dựa trên 10.openapi.yaml này, sinh pytest cho từng endpoint]
```

---

## 5. **Agentic Dev Loop với AI (Cursor IDE, ChatGPT Plus)**

1. Luôn paste prompt/guardrails + spec vào khi mở session AI codegen/testgen.
2. Lặp từng task: 
    - Plan (AI phác backlog, dàn module)
    - Codegen (AI sinh khung, bạn review chỉnh)
    - Test-gen (AI sinh test, convert sang pytest...)
    - Run/build (chạy thử luôn trong IDE, sửa lỗi nếu cần)
    - Review (AI/và peer review re-prompt rule, guardrails)
3. Mọi thay đổi lớn: update ADR, test-case, OpenAPI/contract trước code chi tiết.

---

## 6. Mẫu sử dụng (dùng với ChatGPT Plus/Cursor IDE)

> [ Paste guardrails & prompt ]
> 
> Dựa trên 10.openapi.yaml/glossary này, hãy tạo nhanh skeleton module Python (FastAPI) theo chuẩn coding-standards.md, sinh file test theo test-case. Nếu thiếu info thì bổ sung input cần hỏi.

---

## 7. Tham khảo nâng cao nhanh

- [ADR template (markdown)](https://github.com/npryce/adr-tools/blob/master/doc/adr-template.md)
- [Swagger Editor – design OpenAPI online](https://editor.swagger.io/)
- [Gherkin syntax (for test-case)](https://cucumber.io/docs/gherkin/)
- [PEP8 Python Coding Style](https://peps.python.org/pep-0008/)
- [Agentic Loop giải thích – Github Copilot](https://github.blog/2024-03-28-copilot-workspace-brings-natural-language-to-the-software-development-lifecycle/)

---

## 8. Ghi chú triển khai
- Có thể mở rộng sang bất cứ stack nào (Java, .NET, Node, ...)
- Dành 80% effort đầu chuẩn hóa spec, test, contract trước khi "đổ nghiệp vụ vào code" để AI làm tối ưu nhất.
- Lưu ý: bổ sung manual review, test real-world song song.

## Related



- [MOC_Methodology.md](../../docs/MOC_Methodology.md)
- [d_3.document_strategy_and_creation_order.md](../../docs/methodology/d_3.document_strategy_and_creation_order.md)
- [d_1.quickstarts.md](../../docs/methodology/d_1.quickstarts.md)

## References

- [manifest.yaml](../../docs/grounding/manifest.yaml)
- [manifest.md](../../docs/grounding/manifest.md)
- [methodology.md](../../docs/methodology/methodology.md)
- [d_3.document_strategy_and_creation_order.md](../../docs/methodology/d_3.document_strategy_and_creation_order.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
