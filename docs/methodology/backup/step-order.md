---
id: methodology-backup-step-order
title: Thứ tự tạo tài liệu theo SDLC — Spec-First & Agentic Development
type: runbook
category: methodology
status: published
owner: methodology
domain: methodology
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
- docs/06-quality-assurance/14.qa-matrix.md
- docs/09-runbook/18.dod.md
upstream: []
downstream: []
tags:
  - sdd/methodology
  - sdd/runbook
  - sdd/reusable
  - sdd/generic
- risk/operation
- kms/obsidian
- risk/data
---
# Thứ tự tạo tài liệu theo SDLC — Spec-First & Agentic Development

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Mục đích

Tài liệu này liệt kê thứ tự tạo các tài liệu dựa trên dependencies giữa chúng, theo mô hình **"SDLC mode — Dev theo Spec-First & Agentic Development"** trong `docs/quickstart.md`.

## Nguyên tắc sắp xếp

1. **Tài liệu cần tạo trước được liệt kê trước**: Tài liệu không phụ thuộc vào tài liệu khác được liệt kê đầu tiên
2. **Tài liệu cơ sở được liệt kê trước, tài liệu mới tạo từ tài liệu cơ sở được liệt kê sau**: Tuân thủ quan hệ phụ thuộc (dependencies)

## Thứ tự tạo tài liệu

### Bước 0: Khởi tạo (Framework/Template - nếu chưa có)

**Mục đích**: Các tài liệu khung tri thức (knowledge framework) có thể được tạo trước hoặc dùng từ template của dự án khác. Nếu chưa có, cần tạo trong Bước 2 trước khi tạo các tài liệu thiết kế.

**Thứ tự**:
1. `docs/step-order.md` (nếu chưa có - meta document)
   - **Dependencies**: Không có (meta document về quy trình)
   - **Lý do**: Tài liệu meta hướng dẫn thứ tự tạo các tài liệu khác, nên tạo đầu tiên để định hướng quy trình
   - **Lưu ý**: Có thể dùng từ framework/template và điều chỉnh theo dự án

2. `docs/00-governance/Template/00.cursor-profile.md` (nếu chưa có - template)
   - **Dependencies**: Không có (template document)
   - **Lý do**: Template hướng dẫn tạo `.cursorrules` phù hợp với dự án
   - **Output**: Hướng dẫn và prompt mẫu để tạo `.cursorrules`

3. `.cursorrules` (nếu chưa có - framework/template)
   - **Dependencies**: `docs/00-governance/Template/00.cursor-profile.md` (tham khảo template)
   - **Lý do**: Quy tắc làm việc trong Cursor theo Spec-First & Agentic, cần có sớm để agent tuân thủ
   - **Output**: File `.cursorrules` ở root project
   - **Lưu ý**: Có thể dùng từ framework/template và tùy chỉnh theo dự án

4. `docs/00-governance/00.data-governance.md` (nếu chưa có - framework/template)
   - **Dependencies**: Không có (framework document)
   - **Lý do**: Quy tắc dữ liệu & vai trò, có thể dùng từ framework khác

5. `docs/00-governance/00.governance-rules.md` (nếu chưa có - framework/template)
   - **Dependencies**: Không có (framework document)
   - **Lý do**: Quy tắc governance, có thể dùng từ framework khác

6. `docs/00-governance/00.patterns-anti-patterns.md` (nếu chưa có - framework/template)
   - **Dependencies**: Không có (framework document)
   - **Lý do**: Patterns & anti-patterns, có thể dùng từ framework khác

### Bước 1: Phân tích yêu cầu (Discovery & Requirements)

**Mục đích**: Thu thập bối cảnh, mục tiêu, phạm vi, ràng buộc; xác định use case/module.

**Input**: Đầu bài kinh doanh, stakeholder, dữ liệu sẵn có

**Thứ tự**:
1. `docs/01-requirements/00.requirement.md` ⭐ **TÀI LIỆU ĐẦU TIÊN**
   - **Dependencies**: Không có (từ ticket/stakeholder input)
   - **Output**: Tài liệu yêu cầu dự án
   - **Lý do**: Là điểm khởi đầu, không phụ thuộc vào tài liệu khác

2. `docs/01-requirements/01.use-cases.md`
   - **Dependencies**: `docs/01-requirements/00.requirement.md`
   - **Output**: Use cases và acceptance criteria
   - **Lý do**: Chuẩn hóa yêu cầu thành use case/AC dựa trên requirement

3. `docs/03-business-analysis/02.glossary.md` (nếu có)
   - **Dependencies**: `docs/01-requirements/00.requirement.md`, `docs/01-requirements/01.use-cases.md`
   - **Output**: Business glossary với bounded context
   - **Lý do**: Có thể tạo song song hoặc sau use cases để định nghĩa thuật ngữ

### Bước 2: Thiết kế cấp cao & ràng buộc (High-level Design & Policies)

**Mục đích**: Chọn kiến trúc/biến thể, chuẩn hóa ràng buộc an toàn, dữ liệu, tuân thủ.

**Input**: Tài liệu yêu cầu bước 1; governance documents (nếu chưa có từ framework)

**Thứ tự**:
1. `docs/00-governance/00.data-governance.md` (nếu chưa có từ Bước 0)
   - **Dependencies**: Không có (framework document)
   - **Lý do**: Phải có trước khi tạo tài liệu thiết kế (theo README)

2. `docs/00-governance/00.governance-rules.md` (nếu chưa có từ Bước 0)
   - **Dependencies**: Không có (framework document)
   - **Lý do**: Phải có trước khi tạo tài liệu thiết kế (theo README)

3. `docs/00-governance/00.patterns-anti-patterns.md` (nếu chưa có từ Bước 0)
   - **Dependencies**: Không có (framework document)
   - **Lý do**: Phải có trước khi tạo tài liệu thiết kế (theo README)

4. `docs/02-architecture/03.vision.md`
   - **Dependencies**: `docs/01-requirements/00.requirement.md`, `docs/01-requirements/01.use-cases.md`, `docs/00-governance/00.data-governance.md`, `docs/00-governance/00.governance-rules.md`, `docs/00-governance/00.patterns-anti-patterns.md`
   - **Output**: Tầm nhìn hệ thống, mục tiêu, NFR
   - **Lý do**: Kim chỉ nam cho kiến trúc, cần requirements và governance làm ràng buộc

5. `docs/02-architecture/04.architecture-c4.md`
   - **Dependencies**: `docs/02-architecture/03.vision.md`
   - **Output**: Sơ đồ C4 (Context, Container, Component)
   - **Lý do**: Thiết kế kiến trúc dựa trên vision

6. `docs/02-architecture/05.nfr.md`
   - **Dependencies**: `docs/02-architecture/03.vision.md`
   - **Output**: Non-functional requirements chi tiết
   - **Lý do**: Làm rõ NFR từ vision thành metrics cụ thể

7. `docs/02-architecture/06.adr.md`
   - **Dependencies**: `docs/02-architecture/03.vision.md`, `docs/02-architecture/04.architecture-c4.md`
   - **Output**: Architecture Decision Records
   - **Lý do**: Ghi chép các quyết định kiến trúc quan trọng

8. `docs/02-architecture/08.standards.md`
   - **Dependencies**: `docs/00-governance/00.governance-rules.md`, `docs/02-architecture/03.vision.md`
   - **Output**: Standards và conventions
   - **Lý do**: Chuẩn hóa technical standards dựa trên governance rules và vision

### Bước 3: Business Analysis (nếu có)

**Mục đích**: Mô hình hóa quy trình nghiệp vụ.

**Thứ tự**:
1. `docs/03-business-analysis/09.bpmn.md`
   - **Dependencies**: `docs/01-requirements/01.use-cases.md`, `docs/03-business-analysis/02.glossary.md`
   - **Output**: Business Process Model (BPMN)
   - **Lý do**: Mô hình hóa quy trình từ use cases và glossary

### Bước 4: Spec-First (chuẩn trước, mã sau)

**Mục đích**: Đặc tả hợp đồng API/sự kiện/tool I/O/application/mobile interface để mọi bên bám theo.

**Input**: Yêu cầu + thiết kế; tiêu chuẩn trong governance rules

> **Lưu ý:**  
> - Nếu dự án chỉ có **application/pipeline**: thay cho OpenAPI hãy tạo tài liệu đặc tả application (`docs/04-spec-test/application-spec.md`, mô tả tham số, exit code, log/output schema) và schema cho file/config liên quan.
> - Nếu là **mobile app**: đặc tả API vẫn dùng OpenAPI, nhưng bổ sung mobile contract (navigation, intent, datastore schema) trước khi code.  
> - Nếu có agent tool: dùng JSON schema (`specs/schemas/tools/*.schema.json`).  

**Thứ tự (chọn loại phù hợp dự án)**:
1. `specs/10.openapi.yaml` (khi có REST/HTTP API)
   - **Dependencies**: `docs/01-requirements/01.use-cases.md`, `docs/02-architecture/03.vision.md`, `docs/02-architecture/04.architecture-c4.md`, `docs/00-governance/00.governance-rules.md`
   - **Output**: OpenAPI specification
   - **Lý do**: Đặc tả API contract dựa trên use cases và architecture

2. `docs/04-spec-test/application-spec.md` (đối với application/pipeline)
   - **Dependencies**: Requirements, vision, architecture
   - **Output**: application command spec (tham số, exit code, log schema)
   - **Lý do**: Làm “spec” cho console tool trước khi viết code

3. `specs/schemas/tools/<tool>.schema.json` (nếu có tool hoặc agent)
   - **Dependencies**: Spec (OpenAPI hoặc application) tương ứng
   - **Output**: Tool I/O schemas
   - **Lý do**: Đặc tả tool schemas để agent/code sử dụng thống nhất

### Bước 3b: Project/Code Scaffolding
<mục này có thể sử dụng chatpt để tạo khung cấu trúc theo best practical>
**Mục đích**: Tạo bộ khung thư mục và tệp chuẩn để triển khai nhanh, đồng nhất giữa các dự án. Đảm bảo cấu trúc code phù hợp với team size, complexity, deployment frequency, và các constraints của dự án.

**Input**: Thiết kế cấp cao (bước 2), đặc tả bước 3, governance rules

**Thứ tự**:
1. `docs/02-architecture/07.project-structure.md`
   - **Dependencies**: `docs/02-architecture/03.vision.md`, `docs/02-architecture/04.architecture-c4.md`, `docs/02-architecture/05.nfr.md`, `docs/02-architecture/06.adr.md`, `docs/00-governance/00.governance-rules.md`, `specs/10.openapi.yaml`, `docs/01-requirements/01.use-cases.md`
   - **Output**: Assessment checklist và hướng dẫn thiết kế cấu trúc project (Phase 1-5)
   - **Lý do**: Hướng dẫn chi tiết về việc tạo cấu trúc code project dựa trên assessment (team, deployment, scalability, compliance, technology constraints)

2. Project structure (thư mục và file skeleton)
   - **Dependencies**: `docs/02-architecture/07.project-structure.md`, assessment checklist đã hoàn thành
   - **Output**: Cấu trúc thư mục `src/`, `tests/`, `configs/`, etc.
   - **Lý do**: Structure được tạo dựa trên assessment và architecture decisions

3. Service templates (nếu có nhiều services)
   - **Dependencies**: `docs/02-architecture/07.project-structure.md`, project structure
   - **Output**: Service templates theo pattern đã chọn
   - **Lý do**: Templates để tạo các services mới một cách nhất quán

4. CI/CD configuration (`.github/workflows/ci.yml`)
   - **Dependencies**: `docs/02-architecture/07.project-structure.md`, project structure
   - **Output**: CI/CD pipeline configuration
   - **Lý do**: Automate testing và deployment theo structure

5. Development environment setup (`docs/05-environment-setup/`)
   - **Dependencies**: `docs/02-architecture/07.project-structure.md`, project structure
   - **Output**: Environment setup guides
   - **Lý do**: Hướng dẫn setup môi trường development

### Bước 5: Spec-Test Documentation

**Mục đích**: Tài liệu hướng dẫn về Spec-First và testing.

**Thứ tự**:
1. `docs/04-spec-test/00-assessment-spec-first.md`
   - **Dependencies**: `specs/10.openapi.yaml`, các tài liệu specs và architecture
   - **Output**: Đánh giá tài liệu Spec-First
   - **Lý do**: Đánh giá chất lượng và đầy đủ của spec documents

2. `docs/04-spec-test/10.openapi.yaml` (hoặc application-spec.md)
   - **Dependencies**: `specs/10.openapi.yaml`
   - **Output**: Hướng dẫn về OpenAPI trong Spec-First
   - **Lý do**: Documentation cho OpenAPI spec

3. `docs/04-spec-test/11.governance-jsonschema-rules.md`
   - **Dependencies**: `specs/schemas/tools/*.schema.json`, `docs/00-governance/00.governance-rules.md`
   - **Output**: JSON Schema governance rules
   - **Lý do**: Quy tắc governance cho JSON Schema

4. `docs/04-spec-test/12.governance-spectral.md` 
   - **Dependencies**: `specs/10.openapi.yaml` hoặc specs/schemas/tools/application.schema.json, `docs/00-governance/00.governance-rules.md`
   - **Output**: Spectral lint rules cho OpenAPI
   - **Lý do**: Quy tắc linting cho OpenAPI spec hoặc application spec

### Bước 6: Grounding & Prompts

**Mục đích**: Khai báo nguồn tri thức và prompts để agent bám đúng nguồn.

**Input**: Governance documents (07/13/14), đặc tả ở bước 4

**Thứ tự**:
1. `docs/grounding/manifest.yaml` (nếu chưa có từ framework)
   - **Dependencies**: Không có (framework document, có thể có sẵn)
   - **Output**: Manifest máy-đọc cho Grounding Pack
   - **Lý do**: Có thể dùng từ framework hoặc tạo mới

2. `docs/grounding/manifest.md` (nếu chưa có từ framework)
   - **Dependencies**: `docs/grounding/manifest.yaml`
   - **Output**: Documentation cho manifest
   - **Lý do**: Human-readable documentation cho manifest

3. `docs/grounding/sources.md` (nếu chưa có từ framework)
   - **Dependencies**: `docs/grounding/manifest.yaml`, `docs/00-governance/00.data-governance.md`
   - **Output**: Sources catalog
   - **Lý do**: Catalog các nguồn tri thức

4. `docs/grounding/prompts.md` (nếu chưa có từ framework)
   - **Dependencies**: Không có (framework document)
   - **Output**: Prompt conventions và guidelines
   - **Lý do**: Quy tắc và conventions cho prompts

5. `docs/grounding/evals.md` (nếu chưa có từ framework)
   - **Dependencies**: `docs/grounding/manifest.yaml`, `docs/grounding/sources.md`, `docs/grounding/prompts.md`
   - **Output**: Evaluation guidelines
   - **Lý do**: Hướng dẫn về evaluations và guardrails

6. `docs/grounding/prompts/<module>.md` (per module)
   - **Dependencies**: `docs/grounding/manifest.yaml`, `docs/grounding/sources.md`, `docs/grounding/prompts.md`, `specs/10.openapi.yaml`
   - **Output**: Module-specific prompts
   - **Lý do**: Prompts cho từng module cụ thể

### Bước 7: Quality Assurance

**Mục đích**: Thiết lập QA matrix và prompt guardrails.

**Thứ tự**:
1. `docs/06-quality-assurance/13.prompt-guardrails.md`
   - **Dependencies**: `docs/grounding/prompts.md`, `docs/grounding/manifest.yaml`
   - **Output**: Prompt guardrails policies
   - **Lý do**: Định nghĩa guardrails cho prompts và LLM outputs

2. `docs/06-quality-assurance/14.qa-matrix.md`
   - **Dependencies**: `docs/01-requirements/01.use-cases.md`, `specs/10.openapi.yaml`
   - **Output**: QA Matrix (test types, objectives, responsibilities)
   - **Lý do**: Ma trận QA cho các loại test

### Bước 8: CI/CD & Review

**Mục đích**: Thiết lập quality gates và review checklists.

**Thứ tự**:
1. `docs/07-ci-cd-review/15.quality-matrix.md`
   - **Dependencies**: `docs/06-quality-assurance/14.qa-matrix.md`, `specs/10.openapi.yaml`, `docs/02-architecture/05.nfr.md`
   - **Output**: CI/CD Quality Matrix (thresholds per environment)
   - **Lý do**: Ngưỡng chất lượng cho từng môi trường trong CI/CD

2. `docs/07-ci-cd-review/16.review-checklists.md`
   - **Dependencies**: `docs/00-governance/00.governance-rules.md`, `specs/10.openapi.yaml`
   - **Output**: Review checklists (code, API, security, etc.)
   - **Lý do**: Checklists cho các loại review

### Bước 9: Runbook

**Mục đích**: Định nghĩa Definition of Done và Definition of Ready.

**Thứ tự**:
1. `docs/09-runbook/17.dor.md`
   - **Dependencies**: `docs/01-requirements/01.use-cases.md`, `docs/02-architecture/03.vision.md`
   - **Output**: Definition of Ready (DoR)
   - **Lý do**: Tiêu chí để work item ready để implement

2. `docs/09-runbook/18.dod.md`
   - **Dependencies**: `docs/06-quality-assurance/14.qa-matrix.md`, `docs/07-ci-cd-review/15.quality-matrix.md`
   - **Output**: Definition of Done (DoD)
   - **Lý do**: Tiêu chí để work item được coi là done

### Bước 10: Implementation & Testing (Code & Artifacts)

**Mục đích**: Triển khai code và testing artifacts.

**Thứ tự**:
1. `evals/config.yaml` (nếu chưa có từ framework)
   - **Dependencies**: `docs/grounding/evals.md`, `docs/06-quality-assurance/13.prompt-guardrails.md`
   - **Output**: Evals configuration
   - **Lý do**: Cấu hình cho evaluations

2. `evals/cases/<module>/*.jsonl` (golden/adversarial/regression)
   - **Dependencies**: `evals/config.yaml`, `docs/grounding/manifest.yaml`, `docs/01-requirements/01.use-cases.md`
   - **Output**: Evaluation test cases
   - **Lý do**: Test cases cho evaluations
   NOTE: tạo test cases trước và implement code sau.

3. Code artifacts (skeleton từ bước 3b)
   - `src/<module>/api/`, `src/<module>/services/`, `src/<module>/repositories/`
   - `tests/<module>/*`
   - **Dependencies**: `docs/02-architecture/07.project-structure.md`, project structure, `specs/10.openapi.yaml`, `docs/00-governance/00.governance-rules.md`
   - **Output**: Source code và tests
   - **Lý do**: Implementation theo spec và project structure đã thiết kế

### Bước 11: Handover & Runbook

**Mục đích**: Tài liệu bàn giao và vận hành.

**Thứ tự**:
1. `docs/09-runbook/DEPLOY.md`
   - **Dependencies**: Code artifacts, `specs/10.openapi.yaml`, tests
   - **Output**: Deployment guide
   - **Lý do**: Hướng dẫn deploy

2. `docs/09-runbook/RUNBOOK.md`
   - **Dependencies**: Code artifacts, `specs/10.openapi.yaml`, tests
   - **Output**: Runbook cho operations
   - **Lý do**: Hướng dẫn vận hành

3. `src/<module>/README.md`
   - **Dependencies**: Code artifacts, `specs/10.openapi.yaml`
   - **Output**: Module README
   - **Lý do**: Documentation cho từng module

## Lưu ý quan trọng

### Tài liệu Framework/Template

Các tài liệu sau có thể được tạo từ framework/template của dự án khác và không cần tạo lại cho mỗi dự án:
- `docs/step-order.md` (meta document về quy trình)
- `docs/00-governance/Template/00.cursor-profile.md` (template hướng dẫn tạo `.cursorrules`)
- `docs/00-governance/00.data-governance.md`
- `docs/00-governance/00.governance-rules.md`
- `docs/00-governance/00.patterns-anti-patterns.md`
- `docs/grounding/manifest.yaml`, `docs/grounding/manifest.md`, `docs/grounding/sources.md`, `docs/grounding/prompts.md`, `docs/grounding/evals.md`
- `evals/config.yaml`
- `.cursorrules` (tạo dựa trên `docs/00-governance/Template/00.cursor-profile.md`)
- `.github/workflows/ci.yml`

**Nếu chưa có**, chúng cần được tạo trước hoặc trong các bước tương ứng.

### Tài liệu Project-Specific

Các tài liệu sau là project-specific và phải được tạo cho mỗi dự án:
- `docs/01-requirements/00.requirement.md` ⭐ **TÀI LIỆU ĐẦU TIÊN**
- `docs/01-requirements/01.use-cases.md`
- `docs/03-business-analysis/02.glossary.md`
- `docs/02-architecture/03.vision.md`
- `docs/02-architecture/04.architecture-c4.md`
- `docs/02-architecture/05.nfr.md`
- `docs/02-architecture/06.adr.md`
- `docs/02-architecture/08.standards.md`
- `docs/02-architecture/07.project-structure.md` ⭐ **Hướng dẫn thiết kế cấu trúc code project**
- `docs/03-business-analysis/09.bpmn.md`
- `specs/10.openapi.yaml`
- `specs/schemas/tools/<tool>.schema.json`
- `docs/grounding/prompts/<module>.md`
- `evals/cases/<module>/*.jsonl`
- Code artifacts
- `docs/09-runbook/DEPLOY.md`, `docs/09-runbook/RUNBOOK.md`

## Bảng phụ thuộc chi tiết

> **Xem bảng dependencies chi tiết**: `docs/step-order-dependencies.md`

Bảng dependencies đầy đủ đã được tách ra file riêng để dễ tra cứu và maintain.

## Tham chiếu

- **Căn cứ chính**: `docs/quickstart.md`
- **Bảng dependencies chi tiết**: `docs/step-order-dependencies.md`
- **Tài liệu đầu tiên**: `docs/01-requirements/00.requirement.md`
- **Framework documents**: Có thể dùng từ dự án khác hoặc template

## Related

- [MOC_Spec_First.md](./MOC_Spec_First.md)

## References

- [manifest.yaml](./grounding/manifest.yaml)
- [manifest.md](./grounding/manifest.md)
- [AGENTS.md](../AGENTS.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
