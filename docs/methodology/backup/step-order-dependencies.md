---
id: methodology-backup-step-order-dependencies
title: Bảng phụ thuộc tài liệu — Spec-First & Agentic Development
type: runbook
category: methodology
status: published
owner: runbook
domain: runbook
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
- docs/06-quality-assurance/14.qa-matrix.md
- docs/07-ci-cd-review/16.review-checklists.md
- docs/09-runbook/18.dod.md
- docs/04-spec-test/application-spec.md
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
- agent/verification
---
# Bảng phụ thuộc tài liệu — Spec-First & Agentic Development

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Mục đích

Tài liệu này liệt kê chi tiết dependencies (phụ thuộc) giữa các tài liệu trong SDLC, giúp xác định thứ tự tạo tài liệu chính xác.

> **Xem thêm**: 
> - `docs/step-order.md` - Thứ tự tạo tài liệu (overview)
> - `docs/quickstart.md` - Hướng dẫn từng bước với prompts

## Bảng phụ thuộc tổng quan

| Tài liệu | Dependencies (phải có trước) |
|----------|----------------------------|
| `docs/step-order.md` | Không có (meta document) |
| `docs/00-governance/Template/00.cursor-profile.md` | Không có (template) |
| `.cursorrules` | `docs/00-governance/Template/00.cursor-profile.md` |
| `docs/01-requirements/00.requirement.md` | ⭐ **KHÔNG CÓ** (tài liệu đầu tiên) |
| `docs/01-requirements/01.use-cases.md` | `00.requirement.md` |
| `docs/03-business-analysis/02.glossary.md` | `00.requirement.md`, `01.use-cases.md` |
| `docs/00-governance/00.data-governance.md` | Không có (framework) |
| `docs/00-governance/00.governance-rules.md` | Không có (framework) |
| `docs/00-governance/00.patterns-anti-patterns.md` | Không có (framework) |
| `docs/02-architecture/03.vision.md` | `00.requirement.md`, `01.use-cases.md`, governance docs |
| `docs/02-architecture/04.architecture-c4.md` | `03.vision.md` |
| `docs/02-architecture/05.nfr.md` | `03.vision.md` |
| `docs/02-architecture/06.adr.md` | `03.vision.md`, `04.architecture-c4.md` |
| `docs/02-architecture/08.standards.md` | `00.governance-rules.md`, `03.vision.md` |
| `docs/03-business-analysis/09.bpmn.md` | `01.use-cases.md`, `02.glossary.md` |
| `specs/10.openapi.yaml` | `01.use-cases.md`, `03.vision.md`, `04.architecture-c4.md`, `00.governance-rules.md` |
| `specs/schemas/tools/*.schema.json` | `10.openapi.yaml` |
| `docs/02-architecture/07.project-structure.md` | `03.vision.md`, `04.architecture-c4.md`, `05.nfr.md`, `06.adr.md`, `00.governance-rules.md`, `10.openapi.yaml`, `01.use-cases.md` |
| `docs/04-spec-test/00-assessment-spec-first.md` | `10.openapi.yaml` |
| `docs/04-spec-test/10.openapi.yaml` | `10.openapi.yaml` |
| `docs/04-spec-test/11.governance-jsonschema-rules.md` | `schemas/tools/*.schema.json`, `00.governance-rules.md` |
| `docs/04-spec-test/12.governance-spectral.md` | `10.openapi.yaml`, `00.governance-rules.md` |
| `docs/grounding/manifest.yaml` | Không có (framework) |
| `docs/grounding/manifest.md` | `manifest.yaml` |
| `docs/grounding/sources.md` | `manifest.yaml`, `00.data-governance.md` |
| `docs/grounding/prompts.md` | Không có (framework) |
| `docs/grounding/evals.md` | `manifest.yaml`, `sources.md`, `prompts.md` |
| `docs/grounding/prompts/<module>.md` | `manifest.yaml`, `sources.md`, `prompts.md`, `10.openapi.yaml` |
| `docs/06-quality-assurance/13.prompt-guardrails.md` | `prompts.md`, `manifest.yaml` |
| `docs/06-quality-assurance/14.qa-matrix.md` | `01.use-cases.md`, `10.openapi.yaml` |
| `docs/07-ci-cd-review/15.quality-matrix.md` | `14.qa-matrix.md`, `10.openapi.yaml`, `05.nfr.md` |
| `docs/07-ci-cd-review/16.review-checklists.md` | `00.governance-rules.md`, `10.openapi.yaml` |
| `docs/09-runbook/17.dor.md` | `01.use-cases.md`, `03.vision.md` |
| `docs/09-runbook/18.dod.md` | `14.qa-matrix.md`, `15.quality-matrix.md` |
| `evals/config.yaml` | `evals.md`, `13.prompt-guardrails.md` |
| `evals/cases/<module>/*.jsonl` | `config.yaml`, `manifest.yaml`, `01.use-cases.md` |
| Code artifacts | `docs/02-architecture/07.project-structure.md`, project structure, `10.openapi.yaml`, `00.governance-rules.md` |
| `docs/09-runbook/DEPLOY.md` | Code artifacts, `10.openapi.yaml` |
| `docs/09-runbook/RUNBOOK.md` | Code artifacts, `10.openapi.yaml` |

## Phân loại tài liệu

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

## Tham chiếu

- **Căn cứ chính**: `docs/quickstart.md`
- **Thứ tự tạo tài liệu**: `docs/step-order.md`
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
