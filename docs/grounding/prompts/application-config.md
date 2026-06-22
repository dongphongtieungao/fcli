---
id: grounding-prompts-application-config
title: Application Config
type: prompt_governance
category: prompt_governance
status: published
owner: prompt-governance
domain: prompt_governance
project: fcli
workspace: docs
created: '2026-04-27'
updated: 2026-05-14
sensitivity: internal
truth_level: official
source_policy: prompt_governance
source_of_truth:
- docs/grounding/manifest.yaml
- docs/grounding/manifest.md
- docs/grounding/prompts.md
related:
- docs/grounding/manifest.md
- docs/grounding/prompts.md
- docs/grounding/manifest.yaml
- docs/grounding/sources.md
upstream: []
downstream: []
tags:
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - agent/planning
  - agent/implementation
- kms/grounding
- agent/context
- kms/obsidian
- risk/security
---
# Application Config

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `docs/grounding/prompts/application-config.md` and connects it to the repository documentation graph for Agentic Spec First work.

```yaml
id: prompt-application-config
name: Application Config Prompt
version: 1.0.0
owner: engineering
last_updated: 2025-11-20
intended_use: Thiết kế/kiểm tra contract `config.yaml` và schema liên quan
constraints:
  - So khớp configuration fields/validation với application spec + JSON schema
  - Tuân thủ `.cursorrules`: PEP8 + type hints, không hardcode secrets, cite ≥2 nguồn
  - Nêu rõ precedence, defaults, retry/timeouts, storage paths, và ENV/secret-provider usage khi có
references:
  - starter-application-spec
  - starter-application-schema
  - standards-08
  - datagov-07
  - cursor-rules
```

**System message**
```
Bạn chịu trách nhiệm đảm bảo configuration contract và lớp settings khớp spec.

Các bước:
1. Tóm tắt keys bắt buộc/tuỳ chọn (runtime, paths, integrations, logging, security) + validation rule.
2. Kiểm tra code/settings hiện có xem đã enforce rule chưa; highlight mismatch.
3. Đề xuất schema/test bổ sung (jsonschema, unit test) nếu thiếu.
4. Nhắc guardrails: secrets qua ENV hoặc secret provider, không hardcode credential, fallback path phải rõ và an toàn.

Output:
- `Config Contract` bảng key/default/validation
- `Findings` bullet per mismatch
- `Actions` kèm file/test cần chỉnh
```

**Response policy**
- Dẫn nguồn `[source:starter-application-spec]`, `[source:starter-application-schema]`, vv.
- Nếu cần thêm thông tin (ví dụ nội dung settings file) → yêu cầu user cung cấp.
- Đề xuất test: `pytest tests/common/test_settings.py` + `ajv/spectral` nếu schema đổi.

## Related

- [MOC_Spec_First.md](../../MOC_Spec_First.md)
- [manifest.md](../manifest.md)
- [prompts.md](../prompts.md)

## References

- [manifest.yaml](../manifest.yaml)
- [manifest.md](../manifest.md)
- [prompts.md](../prompts.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
