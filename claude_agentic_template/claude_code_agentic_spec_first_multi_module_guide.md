---
type: runbook
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
  - docs/04-spec-test/cli-spec.md
  - docs/06-quality-assurance/14.qa-matrix.md
  - docs/09-runbook/18.dod.md
related:
  
  
  - docs/04-spec-test/cli-spec.md
  - docs/06-quality-assurance/14.qa-matrix.md
  - docs/09-runbook/18.dod.md
tags:
  - normalized
---
# Hướng dẫn triển khai Claude Code cho dự án multi module theo mô hình Agentic Software Engineering + Spec First GenAI SDLC + Rule + Kiểm tra chân lý + Kiểm soát drift

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `claude_agentic_template/claude_code_agentic_spec_first_multi_module_guide.md` and connects it to the repository documentation graph for Agentic Spec First work.

## 1. Mục tiêu của tài liệu

Tài liệu này là bản hướng dẫn triển khai hoàn chỉnh để bạn dùng Claude Code như công cụ phát triển chính cho một dự án mới hoặc một codebase multi module. Mục tiêu không chỉ là giúp Claude Code đọc repo, mà còn giúp nó làm việc có kỷ luật, ít bịa nghiệp vụ, ít lệch kiến trúc, và luôn phải chứng minh trước khi kết luận task đã hoàn thành.

Tài liệu này hợp nhất ba hướng tư duy thành một hệ thống thống nhất:

1. Agentic Software Engineering, tức là Claude không chỉ sinh code mà còn tham gia vào vòng lặp phân tích, lập kế hoạch, hiện thực, kiểm tra và tự rà soát.
2. Spec First GenAI SDLC, tức là mọi thay đổi đều phải bám vào nguồn sự thật đã được chuẩn hóa như glossary, BPMN, OpenAPI, ADR, NFR, DoR, DoD và task packet.
3. Cơ chế rule + kiểm tra chân lý + kiểm soát drift, tức là không chỉ nhắc Claude phải làm đúng, mà còn có cơ chế cưỡng chế và kiểm tra có thể chứng minh được.

## 2. Tư duy nền tảng

### 2.1. Không xem Claude là người viết code tự do

Claude Code chỉ phát huy tốt khi bạn coi nó như một kỹ sư có hướng dẫn, có runbook, có biên giới module, có nguồn sự thật và có cơ chế kiểm tra đầu ra. Nếu chỉ đưa prompt chung chung, kết quả thường gặp là:

1. bịa nghiệp vụ ngoài spec
2. drift kiến trúc theo từng lần sửa
3. tạo pattern thứ hai trong cùng một module
4. sửa lan sang module khác vì hiểu sai boundary
5. tự tin kết luận đúng chỉ vì compile được

### 2.2. Mô hình đúng là proof based development

Trong hệ thống này, Claude không được mặc định là đúng. Claude phải chứng minh một thay đổi là đúng dựa trên bốn nhóm bằng chứng:

1. đúng theo task packet
2. đúng theo truth source như glossary, BPMN, spec, ADR, NFR
3. có test hoặc kiểm tra phù hợp
4. không vi phạm boundary và không làm drift kiến trúc

## 3. Kiến trúc vận hành tổng thể

Hệ thống hoàn chỉnh nên có 6 lớp.

### 3.1. Lớp 1. Instruction Layer

1. `CLAUDE.md`
2. `.claude/rules/*.md`
3. `src/<module>/CLAUDE.md`

### 3.2. Lớp 2. Ground Truth Layer

1. `docs/02-architecture/01.module-index.md`
2. `docs/02-architecture/02.glossary.md`
3. `docs/02-architecture/03.vision.md`
4. `docs/02-architecture/04.architecture-c4.md`
5. `docs/02-architecture/05.nfr.md`
6. `docs/02-architecture/06.adr.md`
7. `docs/03-business-analysis/09.bpmn.md`
8. `docs/04-spec-test/*`
9. `docs/06-quality-assurance/*`
10. `docs/09-runbook/*`

### 3.3. Lớp 3. Skill Layer

1. `.claude/skills/find-module/SKILL.md`
2. `.claude/skills/plan-task/SKILL.md`
3. `.claude/skills/implement-from-spec/SKILL.md`
4. `.claude/skills/trace-to-truth/SKILL.md`
5. `.claude/skills/detect-drift/SKILL.md`
6. `.claude/skills/preflight-check/SKILL.md`
7. `.claude/skills/proof-report/SKILL.md`

### 3.4. Lớp 4. Task Packet Layer

1. `docs/task-packets/template.md`
2. `docs/task-packets/current.md`
3. `docs/task-packets/archive/*`

### 3.5. Lớp 5. Enforcement Layer

1. `.claude/settings.json`
2. hooks trong Claude Code
3. script kiểm tra
4. CI gate

### 3.6. Lớp 6. Verification Engine

1. `scripts/check_task_packet.py`
2. `scripts/check_truth.py`
3. `scripts/check_module_scope.py`
4. `scripts/check_arch_drift.py`
5. `scripts/check_traceability.py`
6. `scripts/check_adr_needed.py`

## 4. Trình tự tạo project mới

### 4.1. Giai đoạn 1. Tạo lớp điều hướng tối thiểu

1. Tạo `CLAUDE.md`
2. Tạo `docs/02-architecture/02.glossary.md`
3. Tạo `docs/02-architecture/01.module-index.md`

### 4.2. Giai đoạn 2. Tạo lớp module boundary

1. Tạo `src/<module>/CLAUDE.md` cho từng module chính
2. Chốt owned concepts của từng module
3. Chốt not responsible for cho từng module

### 4.3. Giai đoạn 3. Tạo luật chi tiết

1. Tạo `.claude/rules/task-execution.md`
2. Tạo `.claude/rules/truth-check.md`
3. Tạo `.claude/rules/truth-enforcement.md`
4. Tạo `.claude/rules/anti-drift.md`
5. Tạo `.claude/rules/library-selection.md`
6. Tạo `.claude/rules/task-packet-required.md`

### 4.4. Giai đoạn 4. Tạo tầng chân lý

1. Tạo `docs/02-architecture/03.vision.md`
2. Tạo `docs/02-architecture/04.architecture-c4.md`
3. Tạo `docs/02-architecture/05.nfr.md`
4. Tạo `docs/02-architecture/06.adr.md`
5. Tạo `docs/03-business-analysis/09.bpmn.md`
6. Tạo `docs/04-spec-test/*`
7. Tạo `docs/06-quality-assurance/*`
8. Tạo `docs/09-runbook/*`

### 4.5. Giai đoạn 5. Tạo skills

1. Tạo `.claude/skills/find-module/SKILL.md`
2. Tạo `.claude/skills/plan-task/SKILL.md`
3. Tạo `.claude/skills/implement-from-spec/SKILL.md`
4. Tạo `.claude/skills/trace-to-truth/SKILL.md`
5. Tạo `.claude/skills/detect-drift/SKILL.md`
6. Tạo `.claude/skills/preflight-check/SKILL.md`
7. Tạo `.claude/skills/proof-report/SKILL.md`

### 4.6. Giai đoạn 6. Tạo hooks và verification scripts

1. Tạo `.claude/settings.json`
2. Tạo `scripts/check_task_packet.py`
3. Tạo `scripts/check_truth.py`
4. Tạo `scripts/check_module_scope.py`
5. Tạo `scripts/check_arch_drift.py`
6. Tạo `scripts/check_traceability.py`
7. Tạo `scripts/check_adr_needed.py`

## 5. Root CLAUDE.md phải viết gì

### 5.1. Những phần bắt buộc

1. Project Context Summary
2. Module Map Summary
3. Ground Truth Priority
4. Module Discovery Rule
5. Anti Hallucination Rule
6. Scope Discipline
7. Proof Before Change
8. Proof Before Done

### 5.2. Mẫu root CLAUDE.md

```md
# Project Context

Project: Payment Platform
Type: Multi module backend system
Tech stack: Java Spring Boot, Maven multi module, PostgreSQL, REST, SOAP integration

Main goals:
1. manage invoice, payment, refund
2. integrate with external payment providers
3. keep module boundaries clean

# Module Map

1. auth: identity, login, token, access control
2. billing: invoice, payment, refund
3. order: order lifecycle, cart, checkout
4. integration: third party adapters
5. common: shared dto, errors, utilities

# Ground Truth Priority

1. docs/task-packets/current.md
2. docs/04-spec-test/*
3. docs/03-business-analysis/*
4. docs/02-architecture/*
5. src/<module>/CLAUDE.md
6. existing source code

# Module Discovery Rule

When receiving a task:
1. extract business keywords
2. check docs/02-architecture/02.glossary.md
3. identify primary module from glossary
4. read docs/02-architecture/01.module-index.md
5. read src/<module>/CLAUDE.md
6. only then inspect source code

# Anti Hallucination Rule

Do not invent business behavior.

If behavior is unclear:
1. check glossary
2. check BPMN
3. check spec or contract
4. if still unclear, mark Assumption explicitly

# Scope Discipline

1. prefer minimal change
2. avoid cross module refactor unless required
3. do not create a new pattern if the current module already has a stable one
4. keep business logic in the owning module

# Proof Before Change

Before editing code:
1. state task summary
2. identify target module
3. list truth sources to use
4. list assumptions explicitly if data is missing

# Proof Before Done

Before declaring done, provide:
1. task packet used
2. truth sources used
3. files changed
4. tests executed or required
5. architecture impact
6. risks
7. assumptions
```

## 6. Hai file có lợi suất cao nhất trong multi module

### 6.1. `docs/02-architecture/02.glossary.md`

Mục đích:

1. chuẩn hóa thuật ngữ nghiệp vụ
2. gắn mỗi term với một primary module
3. thêm synonym để Claude map task sang module chính xác hơn
4. giảm việc suy luận sai keyword

### 6.2. `docs/02-architecture/01.module-index.md`

Mục đích:

1. cho Claude bản đồ module ở mức tra cứu nhanh
2. gắn keyword với path thực tế trong repo
3. giúp skill find module chạy nhanh và ít sai

## 7. CLAUDE.md cho từng module

### 7.1. Những phần bắt buộc

1. Purpose
2. Owned concepts
3. Not responsible for
4. Entry points
5. External integrations
6. Local rules
7. Common anti patterns
8. Test focus

## 8. Rule files nên thiết kế ra sao

1. `task-execution.md`
2. `truth-check.md`
3. `truth-enforcement.md`
4. `anti-drift.md`
5. `library-selection.md`
6. `task-packet-required.md`

## 9. Ground Truth Pack phải gồm những gì

1. `vision.md`
2. `architecture-c4.md`
3. `nfr.md`
4. `adr.md`
5. `bpmn.md`
6. `spec-test/*`

## 10. Task Packet là trung tâm của mỗi task

Task packet phải có:

1. Task ID
2. Title
3. Why
4. Scope
5. Out of Scope
6. Target Module
7. Truth Sources
8. Acceptance Tests
9. Edge Cases
10. Definition of Done
11. Rollback Plan

## 11. Thiết kế skill theo agentic loop

Bạn nên có các skill sau:

1. `find-module`
2. `plan-task`
3. `implement-from-spec`
4. `trace-to-truth`
5. `detect-drift`
6. `preflight-check`
7. `proof-report`

## 12. Thiết kế hooks và settings

Mục tiêu hook:

1. chặn coding nếu chưa có task packet
2. kiểm tra scope ngay sau chỉnh sửa
3. kiểm tra drift sau thay đổi
4. kiểm tra truth trước khi kết thúc

Mẫu khái niệm `settings.json`:

```json
{
  "hooks": {
    "pre-edit": [
      "python scripts/check_task_packet.py"
    ],
    "post-edit": [
      "python scripts/check_module_scope.py",
      "python scripts/check_arch_drift.py"
    ],
    "pre-submit": [
      "python scripts/check_truth.py",
      "python scripts/check_traceability.py",
      "python scripts/check_adr_needed.py"
    ]
  }
}
```

## 13. Verification Engine phải kiểm tra gì

1. `check_task_packet.py`
2. `check_module_scope.py`
3. `check_truth.py`
4. `check_arch_drift.py`
5. `check_traceability.py`
6. `check_adr_needed.py`

## 14. Cơ chế kiểm soát drift

Drift được chia thành:

1. business drift
2. contract drift
3. architecture drift
4. delivery drift

## 15. Chuỗi vận hành chuẩn cho một task

```text
task
→ docs/task-packets/current.md
→ docs/02-architecture/02.glossary.md
→ docs/02-architecture/01.module-index.md
→ src/<module>/CLAUDE.md
→ docs/03-business-analysis/09.bpmn.md hoặc docs/04-spec-test/*
→ skill plan-task
→ skill implement-from-spec
→ code change
→ hooks and scripts
→ skill preflight-check
→ skill proof-report
→ done
```

## 16. Mapping vào Spec First GenAI SDLC

1. khởi tạo vision, glossary, C4, NFR, ADR
2. mô hình hóa BPMN và acceptance tests
3. chốt contract first
4. implement theo spec
5. chạy QA và proof report

## 17. Mẫu proof report cuối task

```md
# Delivery Proof

## Task Packet Used
[path]

## Target Module
[module]

## Truth Sources Used
1. [path]
2. [path]
3. [path]

## Files Changed
1. [path]
2. [path]

## Tests Run
1. [test or command]
2. [test or command]

## Contract Impact
[none or describe]

## Architecture Impact
[none or describe]

## Risks
1. [risk]
2. [risk]

## Rollback
1. [rollback steps]

## Assumptions
1. [assumption if any]
```

## 18. Bộ tối thiểu để chạy nhanh

1. `CLAUDE.md`
2. `docs/02-architecture/02.glossary.md`
3. `docs/02-architecture/01.module-index.md`
4. `src/<module>/CLAUDE.md`
5. `.claude/rules/task-execution.md`
6. `.claude/rules/truth-check.md`
7. `.claude/rules/truth-enforcement.md`
8. `.claude/rules/anti-drift.md`
9. `.claude/skills/find-module/SKILL.md`
10. `.claude/skills/plan-task/SKILL.md`
11. `docs/task-packets/template.md`
12. `.claude/settings.json`

## 19. Kết luận

Chuỗi đúng để Claude làm việc trong dự án multi module là:

```text
task → task packet → glossary → module index → module CLAUDE.md → spec/BPMN/ADR → skill plan → implement → hook verify → proof report
```

Khi đi đúng chuỗi này, bạn đạt được bốn lợi ích lớn:

1. Claude định vị đúng module nhanh hơn
2. Claude ít bịa nghiệp vụ hơn
3. codebase ít drift hơn theo thời gian
4. mỗi thay đổi có thể chứng minh bằng truth source và test

## Related



- [cli-spec.md](../docs/04-spec-test/cli-spec.md)
- [14.qa-matrix.md](../docs/06-quality-assurance/14.qa-matrix.md)
- [18.dod.md](../docs/09-runbook/18.dod.md)

## References

- [manifest.yaml](../docs/grounding/manifest.yaml)
- [manifest.md](../docs/grounding/manifest.md)
- [cli-spec.md](../docs/04-spec-test/cli-spec.md)
- [14.qa-matrix.md](../docs/06-quality-assurance/14.qa-matrix.md)
- [18.dod.md](../docs/09-runbook/18.dod.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
