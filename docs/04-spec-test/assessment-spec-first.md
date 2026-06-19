---
id: 04-spec-test-assessment-spec-first
title: Assessment Spec First
type: qa_matrix
category: spec_test
status: published
owner: quality-assurance
domain: quality_assurance
project: ftransform
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
related:
- docs/04-spec-test/application-spec.md
- docs/06-quality-assurance/13.prompt-guardrails.md
- docs/07-ci-cd-review/15.quality-matrix.md
upstream:
- docs/04-spec-test/application-spec.md
- docs/06-quality-assurance/13.prompt-guardrails.md
downstream:
- docs/07-ci-cd-review/15.quality-matrix.md
tags:
  - sdd/spec-test
  - sdd/qa
  - sdd/reusable
  - sdd/generic
- sdd/qa
- agent/verification
- kms/obsidian
---
# Assessment Spec First

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

### 0. `00-assessment-spec-first.md`  
**Mục tiêu:**  
- Đánh giá tài liệu Spec-First & Agentic Development để xác định gaps và khuyến nghị cải thiện
- Báo cáo đánh giá và khuyến nghị cải thiện các tài liệu cho Spec-First & Agentic Development workflow

### Mục đích và vai trò

- **Mục đích**: Đánh giá tính đầy đủ và phù hợp của các tài liệu Spec-First & Agentic Development để đảm bảo workflow có thể được thực hiện hiệu quả bởi AI agents và developers. Xác định gaps, điểm mạnh, điểm yếu và đưa ra khuyến nghị cải thiện cụ thể.
- **Vai trò**: Là đầu vào cho việc cải thiện tài liệu; giúp xác định ưu tiên cải thiện (Priority 1, 2, 3); đảm bảo tài liệu phù hợp với Spec-First & Agentic Development workflow.

### Hướng dẫn tạo nội dung

#### 1) Cấu trúc Assessment Report (Format chuẩn)

Mỗi Assessment Report cần có:
- **Tổng quan**: Đối tượng đánh giá (tài liệu nào, mục đích đánh giá)
- **Kết quả đánh giá**: Điểm mạnh (✅) và điểm yếu/thiếu sót (⚠️) cho từng tài liệu
- **Gaps cần bổ sung**: Các gaps chính được nhóm theo chủ đề
- **Khuyến nghị cải thiện**: Phân loại theo mức độ ưu tiên (Priority 1, 2, 3)
- **Tài liệu tham khảo**: Danh sách tài liệu cần tham khảo để hoàn thiện
- **Kết luận**: Tóm tắt và next steps

#### 2) Tiêu chí đánh giá

**Đánh giá từng tài liệu theo các tiêu chí:**
- **Tính đầy đủ**: Tài liệu có đủ thông tin để agent/developer thực hiện task không?
- **Workflow AI Agent**: Có hướng dẫn workflow cho AI agents không?
- **Grounding Pack integration**: Có liên kết với Grounding Pack không?
- **Guardrails enforcement**: Có ruleset thực tế và CI/CD integration không?
- **Spec-First workflow**: Có workflow chi tiết từ requirements → spec → code không?
- **Cross-references**: Có liên kết với tài liệu liên quan không?
- **Examples & Templates**: Có ví dụ và template cụ thể không?

**Đánh giá theo từng nhóm:**
- **Điểm mạnh (✅)**: Những gì tài liệu đã có, đã làm tốt
- **Điểm yếu/Thiếu sót (⚠️)**: Những gì tài liệu thiếu hoặc cần cải thiện

#### 3) Gaps Analysis

**Phân loại gaps:**
- **Workflow gaps**: Thiếu workflow chi tiết cho AI agents
- **Integration gaps**: Thiếu liên kết với Grounding Pack, Guardrails, CI/CD
- **Content gaps**: Thiếu ruleset thực tế, examples, templates
- **Cross-reference gaps**: Thiếu liên kết giữa các tài liệu

**Mô tả gaps:**
- **Thiếu**: Liệt kê những gì thiếu
- **Cần bổ sung**: Liệt kê những gì cần thêm vào

#### 4) Khuyến nghị cải thiện

**Phân loại theo mức độ ưu tiên:**
- **Priority 1 (Quan trọng - làm ngay)**: Các vấn đề critical cần fix ngay
- **Priority 2 (Cải thiện chất lượng)**: Các cải thiện quan trọng nhưng không critical
- **Priority 3 (Tối ưu)**: Các cải thiện optional, nice-to-have

**Format khuyến nghị:**
- Mỗi khuyến nghị nên có: Mô tả ngắn, Tài liệu cần sửa, Action items cụ thể

#### 5) Mapping Assessment → Action Items

**Ánh xạ Assessment → Action Items:**
- Mỗi gap → Action items cụ thể
- Mỗi khuyến nghị → Task assignments
- Priority → Timeline/Deadline

**Ví dụ mapping:**
```
Gap: Workflow AI Agent chưa đầy đủ
  → Action Item 1: Thêm section "Workflow cho AI Agent" vào 10.openapi.yaml
  → Action Item 2: Tạo prompt templates cho từng bước workflow
  → Action Item 3: Cross-reference với docs/06-quality-assurance/13.prompt-guardrails.md
  → Priority: 1
  → Timeline: Sprint 1
```

#### 6) Vai trò của Agent trong tạo Assessment

**Agent có thể**:
- Tự động đánh giá tài liệu dựa trên checklist và criteria
- Phát hiện gaps và thiếu sót trong tài liệu
- Đề xuất khuyến nghị cải thiện dựa trên best practices
- Generate action items từ gaps và khuyến nghị

**Agent không nên**:
- Đánh giá tài liệu không có cơ sở từ criteria/checklist
- Bỏ qua cross-references và integration points
- Tạo khuyến nghị không actionable

#### 7) Prerequisites và Outputs

**Prerequisites (Đầu vào cần có)**:
- Tài liệu cần đánh giá (ví dụ: `10.openapi.yaml`, `06-10.openapi.yaml`, `12.governance-spectral.md`, `11.governance-jsonschema-rules.md`)
- Criteria/Checklist đánh giá (Spec-First & Agentic Development criteria)
- Tài liệu tham khảo: Grounding Pack, Guardrails, Workflow docs
- Assessment template/format

**Outputs (Đầu ra tạo ra)**:
- `docs/04-spec-test/00-assessment-spec-first.md`: Assessment report với kết quả đánh giá, gaps, khuyến nghị
- Action items list: Danh sách action items cụ thể với priority và timeline
- Cross-reference table: Mapping gaps → tài liệu cần sửa → action items

## Tổng quan

Đánh giá 4 tài liệu chính:
- `10.openapi.yaml` - Hướng dẫn sử dụng OpenAPI
- `06-10.openapi.yaml` - Đặc tả API
- `12.governance-spectral.md` - Quy tắc lint OpenAPI
- `11.governance-jsonschema-rules.md` - Quy tắc kiểm tra JSON Schema

## Kết quả đánh giá

### ✅ Điểm mạnh

1. **10.openapi.yaml**
   - ✅ Đã xác định rõ vai trò "ground truth" cho AI agents
   - ✅ Mô tả quy trình spec-first cơ bản
   - ✅ Có ví dụ application và gate policies
   - ⚠️ Thiếu workflow chi tiết cho AI agents
   - ⚠️ Thiếu liên kết trực tiếp với prompt-guardrails

2. **06-10.openapi.yaml**
   - ✅ Có cấu trúc OpenAPI chuẩn
   - ✅ Có examples cho codegen
   - ⚠️ Có lỗi typo (dòng 103: "x")
   - ⚠️ Thiếu security schemes (cho guardrails)
   - ⚠️ Cần thêm metadata cho AI agents

3. **12.governance-spectral.md**
   - ✅ Mô tả rõ vai trò gate CI/CD
   - ✅ Có template và ví dụ
   - ⚠️ Thiếu ruleset thực tế (chỉ có template)
   - ⚠️ Chưa có cross-reference với prompt-guardrails
   - ⚠️ Thiếu workflow cho AI agents khi vi phạm rule

4. **11.governance-jsonschema-rules.md**
   - ✅ Mô tả rõ vai trò validation
   - ✅ Có ví dụ Good/Bad
   - ⚠️ Chưa liên kết với OpenAPI components/schemas
   - ⚠️ Thiếu workflow cho AI agents
   - ⚠️ Chưa có mapping rõ với CI/CD

## Gaps cần bổ sung

### 1. Workflow AI Agent chưa đầy đủ

**Thiếu:**
- Hướng dẫn AI agent đọc và parse OpenAPI
- Quy trình khi AI agent phát hiện discrepancy
- Prompt templates cụ thể cho từng bước
- Integration với Cursor IDE workflow

**Cần bổ sung:**
- Section "Workflow cho AI Agent" trong mỗi tài liệu
- Cross-reference với `docs/06-quality-assurance/13.prompt-guardrails.md`
- Link đến `docs/prompt.txt`

### 2. Grounding Pack integration chưa đầy đủ

**Thiếu:**
- Reference đến `docs/02-architecture/02.glossary.md` trong OpenAPI
- Link đến `docs/02-architecture/08.standards.md` trong governance rules
- Connection với `docs/02-architecture/06.adr.md` cho versioning decisions

**Cần bổ sung:**
- Section "Liên kết với Grounding Pack" trong mỗi tài liệu
- Cross-reference table

### 3. Guardrails chưa được enforce trong tài liệu

**Thiếu:**
- Ruleset Spectral thực tế (chỉ có template)
- Mapping rõ ràng rule → severity → CI/CD gate
- Error handling workflow khi AI agent vi phạm

**Cần bổ sung:**
- Ruleset Spectral đầy đủ trong `12.governance-spectral.md`
- JSON Schema validation rules trong `11.governance-jsonschema-rules.md`
- Workflow "Fix khi vi phạm rule" cho AI agents

### 4. Spec-First workflow chưa chi tiết

**Thiếu:**
- Quy trình từ BPMN → Feature → OpenAPI → Code
- Validation checkpoints
- Rollback strategy khi spec sai

**Cần bổ sung:**
- Workflow diagram/text trong `10.openapi.yaml`
- Integration với `docs/03-business-analysis/09.bpmn.md`

## Khuyến nghị cải thiện

### Priority 1 (Quan trọng - làm ngay)

1. **Sửa lỗi typo** trong `06-10.openapi.yaml` (dòng 103)
2. **Bổ sung workflow AI Agent** vào tất cả 4 tài liệu
3. **Tạo ruleset Spectral thực tế** trong `12.governance-spectral.md`
4. **Thêm cross-references** giữa các tài liệu

### Priority 2 (Cải thiện chất lượng)

1. **Bổ sung security schemes** vào `06-10.openapi.yaml`
2. **Tạo JSON Schema validation rules** cụ thể trong `11.governance-jsonschema-rules.md`
3. **Thêm prompt templates** cho từng bước workflow
4. **Integration với CI/CD** documentation rõ ràng hơn

### Priority 3 (Tối ưu)

1. **Tạo examples** cho AI agents trong mỗi tài liệu
2. **Thêm troubleshooting guide** cho common issues
3. **Tạo checklist** cho từng phase của spec-first workflow

## Tài liệu tham khảo cần đọc

Để hoàn thiện các tài liệu này, cần tham khảo:

1. **Grounding Pack:**
   - `docs/02-architecture/03.vision.md` - Mục tiêu hệ thống
   - `docs/02-architecture/02.glossary.md` - Thuật ngữ nghiệp vụ
   - `docs/02-architecture/08.standards.md` - Coding standards
   - `docs/02-architecture/06.adr.md` - Architecture decisions

2. **Guardrails:**
   - `docs/06-quality-assurance/13.prompt-guardrails.md` - AI guardrails
   - `docs/06-quality-assurance/14.qa-matrix.md` - QA matrix
   - `docs/07-ci-cd-review/15.quality-matrix.md` - CI/CD gates
   - `docs/07-ci-cd-review/16.review-checklists.md` - Review checklist

3. **Workflow:**
   - `docs/00-methodology/methodology.md` - Methodology tổng quan
   - `docs/quickstart.md` - Quickstart guide với catalog tài liệu và workflow
   - `docs/03-business-analysis/09.bpmn.md` - Business flows
   - `docs/prompt.txt` - Prompt templates

4. **CI/CD & Tools:**
   - `docs/07-ci-cd-review/how-to-run-contract-checks.md` - Contract checks
   - `docs/00-governance/00.governance-rules.md` - Governance rules

### Checklist các câu hỏi cần trả lời (làm cơ sở tạo tài liệu)

#### Assessment Scope & Objectives

**Assessment Scope**
- [ ] Đối tượng đánh giá là gì? (tài liệu nào, mục đích đánh giá)
- [ ] Phạm vi đánh giá là gì? (tất cả tài liệu hay chỉ một số tài liệu cụ thể)
- [ ] Mục tiêu đánh giá là gì? (xác định gaps, cải thiện workflow, đảm bảo Spec-First compliance)

**Assessment Objectives**
- [ ] Đánh giá tính đầy đủ của tài liệu? (có đủ thông tin để agent/developer thực hiện task không?)
- [ ] Đánh giá tính phù hợp với Spec-First & Agentic Development? (workflow, integration, guardrails)
- [ ] Đánh giá tính nhất quán? (cross-references, naming conventions, structure)

#### Document Evaluation Criteria

**Tính đầy đủ (Completeness)**
- [ ] Tài liệu có đủ thông tin để agent/developer thực hiện task không?
- [ ] Tài liệu có đủ examples và templates không?
- [ ] Tài liệu có đủ prerequisites và outputs không?

**Workflow AI Agent**
- [ ] Tài liệu có hướng dẫn workflow cho AI agents không?
- [ ] Workflow có chi tiết đủ để agent thực hiện không?
- [ ] Workflow có prompt templates cụ thể không?
- [ ] Workflow có integration với Cursor IDE không?

**Grounding Pack Integration**
- [ ] Tài liệu có liên kết với Grounding Pack không? (`docs/grounding/manifest.yaml`, `docs/grounding/sources.md`)
- [ ] Tài liệu có reference đến glossary không? (`docs/03-business-analysis/02.glossary.md`)
- [ ] Tài liệu có reference đến standards không? (`docs/02-architecture/08.standards.md`)
- [ ] Tài liệu có reference đến ADR không? (`docs/02-architecture/06.adr.md`)

**Guardrails Enforcement**
- [ ] Tài liệu có ruleset thực tế không? (không chỉ template)
- [ ] Tài liệu có mapping rule → severity → CI/CD gate không?
- [ ] Tài liệu có error handling workflow khi vi phạm rule không?
- [ ] Tài liệu có cross-reference với governance rules không? (`docs/00-governance/00.governance-rules.md`)

**Spec-First Workflow**
- [ ] Tài liệu có workflow chi tiết từ requirements → spec → code không?
- [ ] Tài liệu có integration với BPMN flows không? (`docs/03-business-analysis/09.bpmn.md`)
- [ ] Tài liệu có validation checkpoints không?
- [ ] Tài liệu có rollback strategy khi spec sai không?

**Cross-References**
- [ ] Tài liệu có liên kết với tài liệu liên quan không?
- [ ] Cross-references có đầy đủ và chính xác không?
- [ ] Cross-references có được maintain khi có thay đổi không?

**Examples & Templates**
- [ ] Tài liệu có ví dụ cụ thể không?
- [ ] Tài liệu có template có thể sử dụng ngay không?
- [ ] Examples có phản ánh use cases thực tế không?

#### Gaps Analysis

**Workflow Gaps**
- [ ] Có thiếu workflow chi tiết cho AI agents không?
- [ ] Có thiếu hướng dẫn AI agent đọc và parse specs không?
- [ ] Có thiếu quy trình khi AI agent phát hiện discrepancy không?
- [ ] Có thiếu prompt templates cụ thể cho từng bước không?
- [ ] Có thiếu integration với Cursor IDE workflow không?

**Integration Gaps**
- [ ] Có thiếu liên kết với Grounding Pack không?
- [ ] Có thiếu liên kết với Guardrails không?
- [ ] Có thiếu liên kết với CI/CD không?
- [ ] Có thiếu cross-reference với các tài liệu liên quan không?

**Content Gaps**
- [ ] Có thiếu ruleset thực tế không? (chỉ có template)
- [ ] Có thiếu examples cụ thể không?
- [ ] Có thiếu templates có thể sử dụng ngay không?
- [ ] Có thiếu troubleshooting guide không?

**Cross-Reference Gaps**
- [ ] Có thiếu liên kết giữa các tài liệu không?
- [ ] Có thiếu mapping table không? (Spec → Code, Spec → Test, etc.)
- [ ] Có thiếu traceability links không? (requirements → spec → code → test)

#### Recommendations & Action Items

**Priority Classification**
- [ ] Các vấn đề critical nào cần fix ngay? (Priority 1)
- [ ] Các cải thiện quan trọng nào nhưng không critical? (Priority 2)
- [ ] Các cải thiện optional nào? (Priority 3)

**Action Items Definition**
- [ ] Mỗi gap có action items cụ thể không?
- [ ] Mỗi action item có owner không? (ai sẽ làm)
- [ ] Mỗi action item có timeline/deadline không?
- [ ] Action items có được track trong issue tracker không?

**Recommendations Format**
- [ ] Mỗi khuyến nghị có mô tả ngắn không?
- [ ] Mỗi khuyến nghị có tài liệu cần sửa không?
- [ ] Mỗi khuyến nghị có action items cụ thể không?

#### Mapping & Traceability

**Gap → Action Items Mapping**
- [ ] Mỗi gap có map tới action items không?
- [ ] Mapping có rõ ràng và actionable không?
- [ ] Action items có được prioritize không?

**Recommendation → Implementation Mapping**
- [ ] Mỗi khuyến nghị có map tới implementation tasks không?
- [ ] Implementation tasks có được assign không?
- [ ] Implementation tasks có timeline không?

**Document → Improvement Mapping**
- [ ] Mỗi tài liệu có map tới improvements cần thiết không?
- [ ] Improvements có được prioritize không?
- [ ] Improvements có timeline không?

#### Documentation Quality

**Structure & Organization**
- [ ] Assessment report có cấu trúc rõ ràng không?
- [ ] Assessment report có được tổ chức theo logic không?
- [ ] Assessment report có dễ đọc và hiểu không?

**Completeness Check**
- [ ] Assessment report có đánh giá đầy đủ tất cả tài liệu không?
- [ ] Assessment report có xác định đầy đủ gaps không?
- [ ] Assessment report có đưa ra đầy đủ khuyến nghị không?

**Actionability Check**
- [ ] Khuyến nghị có actionable không? (có thể implement được)
- [ ] Action items có cụ thể không? (rõ ràng, đo lường được)
- [ ] Priority có hợp lý không? (critical issues được ưu tiên)

#### Follow-up & Tracking

**Follow-up Plan**
- [ ] Có kế hoạch follow-up sau khi cải thiện không?
- [ ] Có review lại assessment sau khi cải thiện không?
- [ ] Có update assessment report sau khi cải thiện không?

**Tracking & Monitoring**
- [ ] Action items có được track trong issue tracker không?
- [ ] Progress có được monitor không?
- [ ] Completion có được verify không?

### Prompt ví dụ cho Agent: Hoàn thiện nội dung tài liệu Assessment

**Mục đích**: Hướng dẫn Agent tự động hoàn thiện nội dung tài liệu Assessment Report (đánh giá tài liệu Spec-First & Agentic Development) dựa trên câu trả lời từ checklist và các tài liệu cần đánh giá.

**Prompt mẫu**:
```
Bạn là Technical Writer/QA Assistant. Hoàn thiện tài liệu Assessment Report dựa trên câu trả lời checklist và các tài liệu cần đánh giá.

**Bước 1: Thu thập đầu vào**
- Đọc: docs/04-spec-test/00-assessment-spec-first.md (checklist đã trả lời)
- Đọc: Các tài liệu cần đánh giá (ví dụ: 10.openapi.yaml, 06-10.openapi.yaml, 12.governance-spectral.md, 11.governance-jsonschema-rules.md)
- Đọc: Grounding Pack docs (docs/grounding/manifest.yaml, docs/grounding/sources.md)
- Đọc: Guardrails docs (docs/00-governance/00.governance-rules.md, docs/06-quality-assurance/13.prompt-guardrails.md)
- Đọc: Workflow docs (docs/methodology/methodology.md, docs/quickstart.md, docs/step-order.md)
- Đọc: Related docs (docs/02-architecture/03.vision.md, docs/02-architecture/02.glossary.md, docs/02-architecture/08.standards.md, docs/02-architecture/06.adr.md, docs/03-business-analysis/09.bpmn.md)
- Đọc: CI/CD docs (docs/07-ci-cd-review/15.quality-matrix.md, docs/07-ci-cd-review/16.review-checklists.md)

**Bước 2: Phân tích và đánh giá từng tài liệu**
- Với mỗi tài liệu cần đánh giá, đánh giá theo các tiêu chí:
  * Tính đầy đủ (Completeness): Tài liệu có đủ thông tin để agent/developer thực hiện task không?
  * Workflow AI Agent: Có hướng dẫn workflow cho AI agents không?
  * Grounding Pack integration: Có liên kết với Grounding Pack không?
  * Guardrails enforcement: Có ruleset thực tế và CI/CD integration không?
  * Spec-First workflow: Có workflow chi tiết từ requirements → spec → code không?
  * Cross-references: Có liên kết với tài liệu liên quan không?
  * Examples & Templates: Có ví dụ và template cụ thể không?
- Ghi nhận điểm mạnh (✅) và điểm yếu/thiếu sót (⚠️) cho từng tài liệu
- Tổng hợp câu trả lời từ checklist theo các nhóm:
  * Assessment Scope & Objectives: Scope, Objectives
  * Document Evaluation Criteria: Completeness, Workflow AI Agent, Grounding Pack Integration, Guardrails Enforcement, Spec-First Workflow, Cross-References, Examples & Templates
  * Gaps Analysis: Workflow Gaps, Integration Gaps, Content Gaps, Cross-Reference Gaps
  * Recommendations & Action Items: Priority Classification, Action Items Definition, Recommendations Format
  * Mapping & Traceability: Gap → Action Items Mapping, Recommendation → Implementation Mapping, Document → Improvement Mapping
  * Documentation Quality: Structure & Organization, Completeness Check, Actionability Check
  * Follow-up & Tracking: Follow-up Plan, Tracking & Monitoring

**Bước 3: Xác định gaps và phân loại**
- Phân loại gaps theo chủ đề:
  * Workflow gaps: Thiếu workflow chi tiết cho AI agents, hướng dẫn AI agent đọc và parse specs, quy trình khi AI agent phát hiện discrepancy, prompt templates cụ thể, integration với Cursor IDE
  * Integration gaps: Thiếu liên kết với Grounding Pack, Guardrails, CI/CD, cross-reference với các tài liệu liên quan
  * Content gaps: Thiếu ruleset thực tế (chỉ có template), examples cụ thể, templates có thể sử dụng ngay, troubleshooting guide
  * Cross-reference gaps: Thiếu liên kết giữa các tài liệu, mapping table (Spec → Code, Spec → Test, etc.), traceability links (requirements → spec → code → test)
- Mô tả từng gap:
  * Thiếu: Liệt kê những gì thiếu
  * Cần bổ sung: Liệt kê những gì cần thêm vào

**Bước 4: Tạo khuyến nghị cải thiện**
- Phân loại khuyến nghị theo mức độ ưu tiên:
  * Priority 1 (Quan trọng - làm ngay): Các vấn đề critical cần fix ngay (ví dụ: lỗi typo, thiếu workflow AI Agent, thiếu ruleset thực tế, thiếu cross-references)
  * Priority 2 (Cải thiện chất lượng): Các cải thiện quan trọng nhưng không critical (ví dụ: bổ sung security schemes, tạo JSON Schema validation rules, thêm prompt templates, integration với CI/CD)
  * Priority 3 (Tối ưu): Các cải thiện optional, nice-to-have (ví dụ: tạo examples, troubleshooting guide, checklist)
- Format mỗi khuyến nghị:
  * Mô tả ngắn: Vấn đề gì cần cải thiện
  * Tài liệu cần sửa: Tài liệu nào cần được cập nhật
  * Action items cụ thể: Các bước cụ thể để thực hiện

**Bước 5: Tạo action items và mapping**
- Với mỗi gap, tạo action items cụ thể:
  * Action Item: Mô tả cụ thể, rõ ràng, đo lường được
  * Owner: Ai sẽ làm (nếu có)
  * Timeline/Deadline: Khi nào cần hoàn thành
  * Priority: Priority 1, 2, hoặc 3
- Tạo mapping:
  * Gap → Action Items: Mỗi gap map tới các action items cụ thể
  * Recommendation → Implementation: Mỗi khuyến nghị map tới implementation tasks
  * Document → Improvement: Mỗi tài liệu map tới improvements cần thiết
- Đảm bảo action items có thể track trong issue tracker (nếu có)

**Bước 6: Tạo nội dung Assessment Report**
- Sử dụng template trong docs/04-spec-test/00-assessment-spec-first.md
- Điền đầy đủ các phần:
  * Tổng quan: Đối tượng đánh giá (tài liệu nào, mục đích đánh giá)
  * Kết quả đánh giá: Điểm mạnh (✅) và điểm yếu/thiếu sót (⚠️) cho từng tài liệu
  * Gaps cần bổ sung: Các gaps chính được nhóm theo chủ đề (Workflow gaps, Integration gaps, Content gaps, Cross-reference gaps)
  * Khuyến nghị cải thiện: Phân loại theo mức độ ưu tiên (Priority 1, 2, 3) với mô tả, tài liệu cần sửa, action items
  * Tài liệu tham khảo: Danh sách tài liệu cần tham khảo để hoàn thiện (Grounding Pack, Guardrails, Workflow, CI/CD & Tools)
  * Kết luận: Tóm tắt và next steps
- Tạo mapping tables:
  * Gap → Action Items Mapping
  * Recommendation → Implementation Mapping
  * Document → Improvement Mapping

**Bước 7: Review và cải thiện**
- Kiểm tra tính đầy đủ: Assessment report có đánh giá đầy đủ tất cả tài liệu không?
- Kiểm tra tính chính xác: Điểm mạnh/yếu có được mô tả chính xác không? Gaps có được xác định đúng không?
- Kiểm tra tính actionability: Khuyến nghị có actionable không? (có thể implement được)
- Kiểm tra action items: Action items có cụ thể không? (rõ ràng, đo lường được)
- Kiểm tra priority: Priority có hợp lý không? (critical issues được ưu tiên)
- Kiểm tra mapping: Gap → Action Items, Recommendation → Implementation, Document → Improvement có rõ ràng không?
- Kiểm tra cross-references: Tài liệu tham khảo có đầy đủ và chính xác không?
- Liệt kê các điểm TBD (nếu có) và đề xuất câu hỏi làm rõ nếu thiếu thông tin quan trọng.

**Đầu ra**:
1. Tài liệu Assessment Report hoàn chỉnh với kết quả đánh giá, gaps, khuyến nghị
2. Action items list: Danh sách action items cụ thể với priority và timeline
3. Mapping tables: Gap → Action Items Mapping, Recommendation → Implementation Mapping, Document → Improvement Mapping
4. Danh sách điểm TBD (nếu có) và đề xuất câu hỏi làm rõ
5. Liên kết tới tài liệu liên quan (Grounding Pack, Guardrails, Workflow, CI/CD & Tools)
```

**Ví dụ áp dụng**:
```
Bạn là Technical Writer/QA Assistant. Hoàn thiện tài liệu Assessment Report cho các tài liệu Spec-First & Agentic Development.

**Đầu vào** (từ checklist):

**Assessment Scope & Objectives**:
- Đối tượng đánh giá: 10.openapi.yaml, 06-10.openapi.yaml, 12.governance-spectral.md, 11.governance-jsonschema-rules.md
- Phạm vi đánh giá: Đánh giá tính đầy đủ và phù hợp với Spec-First & Agentic Development workflow
- Mục tiêu đánh giá: Xác định gaps, cải thiện workflow, đảm bảo Spec-First compliance

**Document Evaluation Criteria - 10.openapi.yaml**:
- Tính đầy đủ: ✅ Đã xác định rõ vai trò "ground truth" cho AI agents, ⚠️ Thiếu workflow chi tiết cho AI agents
- Workflow AI Agent: ⚠️ Thiếu hướng dẫn AI agent đọc và parse OpenAPI, ⚠️ Thiếu quy trình khi AI agent phát hiện discrepancy
- Grounding Pack Integration: ⚠️ Thiếu reference đến docs/02-architecture/02.glossary.md trong OpenAPI
- Guardrails Enforcement: ⚠️ Thiếu liên kết trực tiếp với prompt-guardrails
- Spec-First Workflow: ✅ Mô tả quy trình spec-first cơ bản, ⚠️ Thiếu workflow chi tiết từ BPMN → Feature → OpenAPI → Code
- Cross-References: ⚠️ Thiếu cross-reference với docs/06-quality-assurance/13.prompt-guardrails.md
- Examples & Templates: ✅ Có ví dụ application và gate policies

**Gaps Analysis**:
1. Workflow AI Agent chưa đầy đủ:
   - Thiếu: Hướng dẫn AI agent đọc và parse OpenAPI, quy trình khi AI agent phát hiện discrepancy, prompt templates cụ thể cho từng bước, integration với Cursor IDE workflow
   - Cần bổ sung: Section "Workflow cho AI Agent" trong tài liệu, cross-reference với docs/06-quality-assurance/13.prompt-guardrails.md, link đến docs/prompt.txt

2. Grounding Pack integration chưa đầy đủ:
   - Thiếu: Reference đến docs/02-architecture/02.glossary.md trong OpenAPI, link đến docs/02-architecture/08.standards.md trong governance rules, connection với docs/02-architecture/06.adr.md cho versioning decisions
   - Cần bổ sung: Section "Liên kết với Grounding Pack" trong mỗi tài liệu, cross-reference table

3. Guardrails chưa được enforce trong tài liệu:
   - Thiếu: Ruleset Spectral thực tế (chỉ có template), mapping rõ ràng rule → severity → CI/CD gate, error handling workflow khi AI agent vi phạm
   - Cần bổ sung: Ruleset Spectral đầy đủ trong 12.governance-spectral.md, JSON Schema validation rules trong 11.governance-jsonschema-rules.md, workflow "Fix khi vi phạm rule" cho AI agents

4. Spec-First workflow chưa chi tiết:
   - Thiếu: Quy trình từ BPMN → Feature → OpenAPI → Code, validation checkpoints, rollback strategy khi spec sai
   - Cần bổ sung: Workflow diagram/text trong 10.openapi.yaml, integration với docs/03-business-analysis/09.bpmn.md

**Khuyến nghị cải thiện**:

**Priority 1 (Quan trọng - làm ngay)**:
1. Sửa lỗi typo trong 06-10.openapi.yaml (dòng 103)
   - Tài liệu cần sửa: 06-10.openapi.yaml
   - Action items: Sửa lỗi typo "x" thành giá trị đúng

2. Bổ sung workflow AI Agent vào tất cả 4 tài liệu
   - Tài liệu cần sửa: 10.openapi.yaml, 06-10.openapi.yaml, 12.governance-spectral.md, 11.governance-jsonschema-rules.md
   - Action items:
     * Thêm section "Workflow cho AI Agent" vào mỗi tài liệu
     * Tạo prompt templates cho từng bước workflow
     * Cross-reference với docs/06-quality-assurance/13.prompt-guardrails.md
     * Link đến docs/prompt.txt

3. Tạo ruleset Spectral thực tế trong 12.governance-spectral.md
   - Tài liệu cần sửa: 12.governance-spectral.md
   - Action items:
     * Tạo ruleset Spectral đầy đủ với các rules cụ thể
     * Map rule → severity → CI/CD gate
     * Tạo workflow "Fix khi vi phạm rule" cho AI agents

4. Thêm cross-references giữa các tài liệu
   - Tài liệu cần sửa: Tất cả 4 tài liệu
   - Action items:
     * Thêm section "Liên kết với Grounding Pack" trong mỗi tài liệu
     * Thêm cross-reference table
     * Link đến docs/02-architecture/02.glossary.md, docs/02-architecture/08.standards.md, docs/02-architecture/06.adr.md

**Priority 2 (Cải thiện chất lượng)**:
1. Bổ sung security schemes vào 06-10.openapi.yaml
   - Tài liệu cần sửa: 06-10.openapi.yaml
   - Action items: Thêm security schemes (OAuth2, API Key, etc.) cho guardrails

2. Tạo JSON Schema validation rules cụ thể trong 11.governance-jsonschema-rules.md
   - Tài liệu cần sửa: 11.governance-jsonschema-rules.md
   - Action items: Tạo JSON Schema validation rules cụ thể, link với OpenAPI components/schemas

3. Thêm prompt templates cho từng bước workflow
   - Tài liệu cần sửa: 10.openapi.yaml, 12.governance-spectral.md, 11.governance-jsonschema-rules.md
   - Action items: Tạo prompt templates cho từng bước workflow (read spec, parse spec, validate spec, fix issues)

4. Integration với CI/CD documentation rõ ràng hơn
   - Tài liệu cần sửa: 12.governance-spectral.md, 11.governance-jsonschema-rules.md
   - Action items: Thêm section "CI/CD Integration" với mapping rõ ràng rule → CI/CD gate

**Priority 3 (Tối ưu)**:
1. Tạo examples cho AI agents trong mỗi tài liệu
   - Tài liệu cần sửa: Tất cả 4 tài liệu
   - Action items: Thêm examples cụ thể cho AI agents sử dụng

2. Thêm troubleshooting guide cho common issues
   - Tài liệu cần sửa: 10.openapi.yaml, 12.governance-spectral.md, 11.governance-jsonschema-rules.md
   - Action items: Tạo section "Troubleshooting" với common issues và solutions

3. Tạo checklist cho từng phase của spec-first workflow
   - Tài liệu cần sửa: 10.openapi.yaml
   - Action items: Tạo checklist cho từng phase (BPMN → Feature → OpenAPI → Code)

**Mapping**:
- Gap "Workflow AI Agent chưa đầy đủ" → Action Items: Thêm section "Workflow cho AI Agent", Tạo prompt templates, Cross-reference với prompt-guardrails, Link đến prompt.txt
- Gap "Grounding Pack integration chưa đầy đủ" → Action Items: Thêm section "Liên kết với Grounding Pack", Cross-reference table, Link đến glossary, standards, ADR
- Gap "Guardrails chưa được enforce" → Action Items: Tạo ruleset Spectral thực tế, Map rule → severity → CI/CD gate, Tạo workflow "Fix khi vi phạm rule"
- Gap "Spec-First workflow chưa chi tiết" → Action Items: Workflow diagram/text, Integration với BPMN flows, Validation checkpoints, Rollback strategy

**Đầu ra**:
1. Assessment Report hoàn chỉnh với kết quả đánh giá, gaps, khuyến nghị
2. Action items list với priority và timeline
3. Mapping tables: Gap → Action Items, Recommendation → Implementation, Document → Improvement
4. Liên kết tới tài liệu tham khảo
```

**Lưu ý khi sử dụng prompt**:
- Prompt này có thể được sử dụng bởi Agent (ChatGPT Plus, Cursor IDE) để tự động hoàn thiện Assessment Report dựa trên checklist đã trả lời.
- Agent nên đọc tất cả tài liệu cần đánh giá và tài liệu liên quan trước khi đánh giá.
- Agent nên đánh giá từng tài liệu theo các tiêu chí và ghi nhận điểm mạnh/yếu một cách khách quan.
- Agent nên xác định gaps và phân loại chúng theo chủ đề.
- Agent nên tạo khuyến nghị cải thiện với priority và action items cụ thể.
- Agent nên tạo mapping giữa gaps → action items, recommendation → implementation, document → improvement.
- Sau khi hoàn thiện, Agent nên review lại để đảm bảo tính đầy đủ, chính xác, và actionability.

## Kết luận

Các tài liệu đã có nền tảng tốt nhưng cần:
1. **Bổ sung workflow chi tiết** cho AI agents
2. **Tạo ruleset thực tế** thay vì chỉ template
3. **Tăng cường cross-references** giữa các tài liệu
4. **Integration rõ ràng** với Grounding Pack và Guardrails

Sau khi cải thiện, các tài liệu sẽ phù hợp với "Spec-First & Agentic Development" workflow và hỗ trợ tốt cho Cursor IDE + ChatGPT Plus.

## Related

- [MOC_Spec_First.md](../MOC_Spec_First.md)

## References

- [manifest.yaml](../grounding/manifest.yaml)
- [manifest.md](../grounding/manifest.md)
- [AGENTS.md](../../AGENTS.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
