---
id: 07-ci-cd-review-how-to-run-contract-checks
title: How to run contract checks (Spec-First)
type: runbook
category: ci_cd_review
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
- docs/methodology/methodology.md
- docs/methodology/d_3.document_strategy_and_creation_order.md
- docs/03-business-analysis/09.bpmn.md
related:
- docs/03-business-analysis/09.bpmn.md
- tests/bdd/README.md
- docs/06-quality-assurance/13.prompt-guardrails.md
- docs/06-quality-assurance/14.qa-matrix.md
- docs/04-spec-test/application-spec.md
- docs/07-ci-cd-review/15.quality-matrix.md
- docs/09-runbook/18.dod.md
upstream:
- docs/04-spec-test/application-spec.md
- docs/06-quality-assurance/13.prompt-guardrails.md
- docs/06-quality-assurance/14.qa-matrix.md
- docs/09-runbook/18.dod.md
downstream:
- docs/07-ci-cd-review/15.quality-matrix.md
tags:
  - sdd/review
  - sdd/runbook
  - sdd/reusable
  - sdd/generic
- risk/operation
- kms/obsidian
- risk/performance
- risk/data
---
# How to run contract checks (Spec-First)

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `docs/07-ci-cd-review/how-to-run-contract-checks.md` and connects it to the repository documentation graph for Agentic Spec First work.

## 1) OpenAPI lint (Spectral)
- Mục tiêu: Kiểm tra tuân thủ OAS theo rule nội bộ
- Lệnh:
```bash
spectral lint docs/04-spec-test/06-10.openapi.yaml \
  -r docs/04-spec-test/12.governance-spectral.md
```
- Gate gợi ý:
  - PR: warn-only (không có error)
  - Release: fail-on-error (không có warn/error core)

## 2) Render tài liệu (Redoc – tùy chọn)
```bash
OpenAPI documentation tool bundle docs/04-spec-test/06-10.openapi.yaml -o reports/openapi.html
```

## 3) JSON Schema validation (Ajv validation tool – ví dụ)
- Mục tiêu: Validate payloads mẫu theo JSON Schema (nếu dùng riêng)
- Ví dụ (giả sử có schema và sample JSON):
```bash
# Cài đặt (nếu cần): npm i -g ajv validation package
ajv validate -s schemas/report-summary.schema.json -d samples/report-summary.json \
  --spec=draft2020 --strict=true --all-errors=false
```

## 4) Contract test tự động (gợi ý tools)
- Schemathesis (HTTP): sinh test từ OAS, fuzz cơ bản
```bash
# Cài đặt: pip install schemathesis
schemathesis run --checks all --hypothesis-max-examples=20 \
  --base-url http://localhost:8000 docs/04-spec-test/06-10.openapi.yaml
```
- openapi-core/prance (parse/validate OAS) – dùng trong unit/integration tùy chọn

## 5) Mapping vào CI/CD (tham khảo)
- PR job (nhẹ):
  - spectral lint (warn-only)
  - render docs (artifact)
- Nightly/Release job (đầy đủ):
  - spectral lint (strict fail)
  - schemathesis run (subset endpoints nếu cần)
  - ajv validate cho schemas trọng yếu

## 6) Chính sách lỗi gợi ý
- Core rules lỗi (error): operationId unique; thiếu response examples 2xx/4xx; thiếu description quan trọng
- Warn: thiếu info-contact/tags; docs chưa đủ mô tả

## 7) Liên kết tài liệu
- OpenAPI: docs/04-spec-test/10.openapi.yaml, docs/04-spec-test/06-10.openapi.yaml
- Ruleset: docs/04-spec-test/12.governance-spectral.md, docs/04-spec-test/11.governance-jsonschema-rules.md
- QA Matrix: docs/06-quality-assurance/14.qa-matrix.md

## Related

- [MOC_Spec_First.md](../MOC_Spec_First.md)
- [09.bpmn.md](../03-business-analysis/09.bpmn.md)
- [README.md](../../tests/bdd/README.md)
- [13.prompt-guardrails.md](../06-quality-assurance/13.prompt-guardrails.md)
- [14.qa-matrix.md](../06-quality-assurance/14.qa-matrix.md)

## References

- [manifest.yaml](../grounding/manifest.yaml)
- [manifest.md](../grounding/manifest.md)
- [methodology.md](../methodology/methodology.md)
- [d_3.document_strategy_and_creation_order.md](../methodology/d_3.document_strategy_and_creation_order.md)
- [09.bpmn.md](../03-business-analysis/09.bpmn.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
