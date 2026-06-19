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
# Tổng quan cấu trúc tài liệu – Spec-First GenAI SDLC (Quick Reference)

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `claude_agentic_template/methodology/0_overview.md` and connects it to the repository documentation graph for Agentic Spec First work.

Tài liệu này mô tả **vai trò, mục tiêu sử dụng và nội dung cần lưu của từng nhóm tài liệu đã được quy hoạch** cho dự án, tối ưu cho việc agent (AI/Cursor IDE) tự động tham chiếu – review – grounding – kiểm soát chất lượng, giảm technical debt và hỗ trợ onboarding nhanh cho thành viên mới.

---

## 1. Grounding Pack (Nguồn sự thật cho AI/codegen)
| File/Tệp                       | Mục tiêu – Nội dung cần lưu |
|--------------------------------|---------------------------|
| `/docs/02-architecture/03.vision.md`       | **Tầm nhìn, mục tiêu hệ thống**: Giải thích “system purpose” hoặc business goal làm kim chỉ nam cho toàn kiến trúc, AI/codegen cần hiểu rõ để không lệch hướng. |
| `/docs/02-architecture/04.architecture-c4.md` | **Sơ đồ kiến trúc C4**: Biểu đồ tổng thể system/component/context, agent scan để automatic codegen hoặc validate ràng buộc hệ. |
| `/docs/02-architecture/02.glossary.md` | **Thuật ngữ/domain DDD**: Định nghĩa từ khóa/nghiệp vụ, chuẩn hóa để agent sinh code/test, giảm hiểu lầm nghiệp vụ. |
| `/docs/02-architecture/08.standards.md`      | **Coding standards**: Quy chuẩn về phong cách, logging, error, exception, naming... cho mọi module. AI/codegen phải tuân thủ. |
| `/docs/02-architecture/05.nfr.md`            | **Non-functional requirements**: Yêu cầu phi chức năng (security, performance, scalability...) – là ràng buộc để agent tạo code/test sát thực tế vận hành. |
| `/docs/02-architecture/06.adr.md`            | **Architecture Decision Records**: Ghi nhận quyết định kiến trúc – lý do, trade-off, design choice để AI/gen code đúng strategy và lịch sử lý do. |
| `/docs/04-spec-test/01-10.openapi.yaml`         | **Đặc tả API – OpenAPI schema**: Grounding contract cho code/test auto (không bịa API ngoài schema này). |
| `/docs/00-governance/00.data-governance.md`  | **Chính sách quản trị dữ liệu**: Policy cho storage, data access, retention... Làm căn cứ kiểm soát AI/gen code mọi thao tác data. |
| `/docs/03-business-analysis/09.bpmn.md`      | **BPMN quy trình nghiệp vụ**: Flow nghiệp vụ, giúp AI tạo/sequencing test, detect thiếu logic khi implement code. |

## 2. Guardrails & Quality Assurance (Bảo vệ chất lượng và technical debt)
| File/Tệp                                 | Mục tiêu – Nội dung cần lưu |
|------------------------------------------|----------------------------|
| `/docs/06-quality-assurance/13.prompt-guardrails.md` | **Guardrails/prompt mẫu cho AI**: Rule, lời nhắc/kịch bản để ngăn AI/agent sinh code "bịa", enforce tiêu chuẩn nghiệp vụ/kỹ thuật. |
| `/docs/06-quality-assurance/14.qa-matrix.md`           | **QA matrix**: Checklist test types, chất lượng mã – agent/reviewer check nhanh từng release. |
| `/docs/09-runbook/18.dod.md` & `/17.dor.md`         | **DoD, DoR**: Điều kiện tối thiểu để dev/agent được bàn giao chuyển pha (chốt task/deliverable), giúp automation/PR gating đúng.

## 3. Governance & Patterns (Quy tắc và mẫu phát triển)
| File/Tệp                                 | Mục tiêu – Nội dung cần lưu |
|------------------------------------------|----------------------------|
| `/docs/00-governance/00.governance-rules.md`       | **Rule, policy tổng về governance**: Meta rule cho mọi loại contract/code/test (OpenAPI, JSON Schema...), aid cho automation checker. |
| `/docs/00-governance/00.patterns-anti-patterns.md` | **Best/Anti Pattern**: Case mẫu tốt/xấu để tránh technical debt, agent hoặc reviewer dễ cross-check khi review. |

## 4. Review, Test & CI/CD Checklist
| File/Tệp                                | Mục tiêu – Nội dung cần lưu |
|-----------------------------------------|----------------------------|
| `/docs/07-ci-cd-review/15.quality-matrix.md`       | **Ma trận chất lượng kiểm thử–CI/CD**: Tiêu chí gating, coverage, mutation score, review AI/manual. |
| `/docs/07-ci-cd-review/16.review-checklists.md`    | **Code review checklist chuẩn**: Từng hạng mục reviewer/agent phải làm khi duyệt code, enforce technical debt policy. |

## 5. Runbook, Handover & Operation
| File/Tệp                                | Mục tiêu – Nội dung cần lưu |
|-----------------------------------------|----------------------------|
| `/docs/09-runbook/18.dod.md, 17.dor.md`           | **Checklist DoD, DoR**: Lặp lại điều kiện chuyển pha/ban giao (cho dev & agentic automation). |

---
> **Cách sử dụng cho Agent/AI/Onboard dev**: Chỉ cần truy đúng nhánh này là đủ thông tin chuẩn – agent automation đảm bảo full traceability (spec → code → test → review → prod), rào kỹ mọi technical debt bằng QA/checklist chuẩn.
> 
> **Lưu ý:** Nếu phát sinh thêm contract/test loại mới, luôn ghi rõ/extension tại vị trí thư mục chuẩn này để mọi người và AI đều đồng bộ.

---

## 6. Trình tự thực hiện (Spec‑First & Agentic Development)

1) Kiến trúc tổng quan (Grounding Pack)
   - Thực hiện: xây `docs/02-architecture/03.vision.md`, `04.architecture-c4.md`, `02.glossary.md`, `08.standards.md`, `05.nfr.md`, `06.adr.md`.
   - Điều kiện thực hiện:
     - Đầu vào tối thiểu: mô tả sản phẩm/nghiệp vụ ở mức tóm tắt (stakeholder brief).
   - Tài liệu điều kiện: `docs/02-architecture/03.vision.md` (draft), `02.glossary.md` (khung thuật ngữ).
   - Output:
     - Tầm nhìn/KPIs, sơ đồ C4 (context/container), từ điển thuật ngữ (Owner/Source), chuẩn code & rule mapping, NFR theo môi trường, ADR-xxx trạng thái.

2) Dựng khung kiến trúc dự án (Scaffold)
   - Thực hiện: đồng bộ cấu trúc thư mục mã nguồn/templates/sql/mock/reports theo C4/NFR.
   - Điều kiện thực hiện:
     - Có `docs/02-architecture/04.architecture-c4.md` (draft Context/Container) và `docs/02-architecture/05.nfr.md` (ngưỡng khung).
   - Tài liệu điều kiện: `04.architecture-c4.md`, `05.nfr.md`.
   - Output:
     - Khung module `src/` và `tests/`, templates `.pptx`, script `sql/slide*.sql`, mock CSV, cấu hình `config.yaml` (nếu cần).

3) Tài liệu nghiệp vụ (03-business-analysis)
   - Thực hiện: cập nhật `docs/03-business-analysis/09.bpmn.md` (Flow ID/Step ID/Actor, validation points, PlantUML snippet, seed data), và tạo ví dụ tại `docs/03-business-analysis/Ex/`.
   - Điều kiện thực hiện:
     - Có `03.vision.md` (mục tiêu), `02.glossary.md` (thuật ngữ) để chuẩn hóa tên flow/step/actor.
   - Tài liệu điều kiện: `03.vision.md`, `02.glossary.md`.
   - Output:
     - Các flow có happy path + error path, cross-link tới OpenAPI/Schema/SQL/Testcase, checklist DoF.
    
    NOTE: BPMN → .feature & BDD (draft) → Spec/Contract → Guardrails/QA → Runbook.
4) Tạo .feature & BDD (Executable Specification, draft từ BPMN)
   - Thực hiện: sinh kịch bản từ BPMN → `.feature` tại `tests/features/`, scaffold step definitions `tests/bdd/` (pytest‑bdd/behave).
   - Điều kiện tạo (pre-conditions):
     - Flow trong `docs/03-business-analysis/09.bpmn.md` đã có Flow ID, Step ID, Actor, entry/exit, happy path + ít nhất 1 error path.
     - Cross-link tối thiểu: Step ↔ OpenAPI/Schema/SQL (nếu áp dụng), có seed/mock dữ liệu cần thiết.
   - Tài liệu điều kiện: `docs/03-business-analysis/09.bpmn.md`, `mock/csv/`, `sql/`, `templates/`.
   - Output cụ thể:
     - Thư mục `tests/features/` với file `.feature` theo từng flow (ví dụ: `generate_monthly_report.feature`).
     - Thư mục `tests/bdd/` với step definitions tương ứng (đánh dấu pending nếu chưa implement) để test được thu thập ngay.
   - Vai trò (mục đích):
     - Đóng vai trò “Executable Specification” – tiêu chí chấp nhận chạy được, làm ground truth cho dev/agent khi hiện thực mã.
   - Liên kết bước tiếp theo:
     - Dẫn tới quá trình hoàn thiện Contract/Spec (áp rule lint) và hiện thực code, chuyển dần test từ pending → pass.

5) Contract/Spec (Spec‑First)
   - Thực hiện: cập nhật `docs/04-spec-test/01-10.openapi.yaml`, `10.openapi.yaml`, rule `12.governance-spectral.md`, `11.governance-jsonschema-rules.md`.
   - Điều kiện thực hiện:
     - `.feature` đã phác IO/hành vi; với API step phải có/hoặc chốt `01-10.openapi.yaml` trước khi implement.
   - Tài liệu điều kiện: `tests/features/*.feature`, `docs/04-spec-test/01-10.openapi.yaml`.
   - Output:
     - OpenAPI versioned, rule lint (Spectral), rule JSON Schema, ví dụ request/response phục vụ mock/test.

6) Guardrails & QA
   - Thực hiện: `docs/06-quality-assurance/13.prompt-guardrails.md`, `14.qa-matrix.md`; CI/CD matrix `docs/07-ci-cd-review/15.quality-matrix.md`, checklist review `16.review-checklists.md`.
   - Điều kiện thực hiện:
     - Có `08.standards.md` và Contract/Spec để cấu hình lint/contract test; xác định ngưỡng NFR tối thiểu.
   - Tài liệu điều kiện: `docs/02-architecture/08.standards.md`, `docs/04-spec-test/01-10.openapi.yaml`, `docs/02-architecture/05.nfr.md`.
   - Output:
     - Guardrails cho AI/agent, ma trận test/gate, checklist reviewer, mapping job CI.

7) Runbook & Handover
   - Thực hiện: DoR/DoD `docs/09-runbook/17.dor.md`, `18.dod.md`.
   - Điều kiện thực hiện:
     - Có QA matrix/gates, guardrails và Spec để định nghĩa tiêu chí chuyển pha.
   - Tài liệu điều kiện: `docs/06-quality-assurance/14.qa-matrix.md`, `docs/07-ci-cd-review/15.quality-matrix.md`, `docs/06-quality-assurance/13.prompt-guardrails.md`.
   - Output:
     - Checklist chuyển pha, điều kiện bàn giao, liên kết CI gate.

Ghi chú: Có thể bỏ qua phân tích yêu cầu chi tiết nếu đã có domain/flow cốt lõi; ưu tiên Spec‑First + Guardrails để AI scaffold và sinh test sớm, giảm drift và nợ kỹ thuật ngay từ PR đầu tiên.

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
