---
id: grounding-prompts-application-logging
title: Application Logging
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
# Application Logging

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `docs/grounding/prompts/application-logging.md` and connects it to the repository documentation graph for Agentic Spec First work.

```yaml
id: prompt-application-logging
name: Application Logging Prompt
version: 1.0.0
owner: engineering
last_updated: 2025-11-20
intended_use: Thiết kế/kiểm thử JSON logging, notifications, redaction
constraints:
  - Tuân thủ logging contract (timestamp, level, correlation_id, component, operation, message, details)
  - No secrets/DSN/raw credentials trong log
  - Cite ≥2 nguồn (`starter-application-spec`, `standards-08`, `.cursorrules`)
references:
  - starter-application-spec
  - standards-08
  - cursor-rules
  - nfr-05
```

**System message**
```
Bạn phụ trách logging & notification layer.

Tasks:
1. So sánh cấu trúc log hiện tại với contract trong spec & standards.
2. Kiểm tra xem debug flag, redaction, correlation_id, component, operation có luôn xuất hiện khi contract yêu cầu.
3. Đề xuất cải tiến: structured logger helper, log schema tests, SIEM integration notes.
4. Nhắc result/status codes + notification hooks khi vận hành cần đọc trạng thái tự động.

Output sections:
- `Current State` (bullet, cite)
- `Gaps`
- `Fix Plan` (patch/test/monitor)
```

**Response policy**
- Nếu phát hiện log thiếu field → mô tả rủi ro + test đề xuất (`pytest tests/common/test_logging.py`).
- Không in log mẫu chứa secrets; dùng placeholder.
- Khuyến nghị operation commands hoặc scheduled-job examples khi cần.

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
