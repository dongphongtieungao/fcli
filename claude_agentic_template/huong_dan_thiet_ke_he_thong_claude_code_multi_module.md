---
type: technical_note
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
# Hướng dẫn xây hệ thống Claude Code cho dự án multi-module

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Mục tiêu tài liệu

Tài liệu này hướng dẫn cách thiết kế một hệ thống file và quy ước vận hành để Claude Code có thể:

1. hiểu đúng bối cảnh dự án
2. định vị đúng module nghiệp vụ
3. không bịa nghiệp vụ ngoài đặc tả
4. xử lý task theo quy trình lặp lại được
5. tự kiểm trước khi kết thúc

Tài liệu này tập trung vào 2 câu hỏi chính:

A. Khi tạo một project mới, cần tạo những file gì và theo thứ tự nào  
B. Claude sẽ đọc gì, suy luận gì, và dựa vào đâu để xác định module, phân tích, xử lý và verify

---

## 1. Tư duy cốt lõi

Hãy xem hệ thống Claude Code của bạn gồm 4 lớp:

## Lớp 1. Luật nền
Mục tiêu là nói cho Claude biết dự án này là gì, có module nào, phải đọc gì trước, không được bịa gì.

Các file chính:
1. `CLAUDE.md`
2. `src/<module>/CLAUDE.md`
3. `.claude/rules/*.md`

## Lớp 2. Chân lý dự án
Mục tiêu là cho Claude nơi tra cứu nghĩa nghiệp vụ, kiến trúc, flow và contract.

Các file chính:
1. `docs/02-architecture/01.module-index.md`
2. `docs/02-architecture/02.glossary.md`
3. `docs/02-architecture/04.architecture-c4.md`
4. `docs/02-architecture/06.adr.md`
5. `docs/03-business-analysis/09.bpmn.md`
6. `docs/04-spec-test/*`

## Lớp 3. Playbook
Mục tiêu là khi gặp một loại task quen thuộc, Claude có một runbook để làm theo.

Các file chính:
1. `.claude/skills/find-module/SKILL.md`
2. `.claude/skills/plan-task/SKILL.md`
3. `.claude/skills/implement-from-spec/SKILL.md`
4. `.claude/skills/preflight-check/SKILL.md`

## Lớp 4. Cưỡng chế và kiểm tra
Mục tiêu là không chỉ nhắc Claude, mà còn buộc Claude tự kiểm.

Các file chính:
1. `.claude/settings.json`
2. `scripts/check_task_packet.py`
3. `scripts/check_truth.py`
4. `scripts/check_module_scope.py`

---

## 2. Claude Code hiểu những path nào là path đặc biệt

Đây là điểm rất quan trọng.

## 2.1 Path đặc biệt mà Claude Code thực sự hiểu

### Memory và instructions
1. `~/.claude/CLAUDE.md`
2. `./CLAUDE.md`
3. `./.claude/CLAUDE.md`
4. `<any-folder>/CLAUDE.md`
5. `.claude/rules/*.md`

### Skills
1. `.claude/skills/<skill-name>/SKILL.md`

### Settings và hooks
1. `.claude/settings.json`
2. `.claude/settings.local.json`

## 2.2 Những path không phải special path
Các thư mục sau không có ý nghĩa đặc biệt theo mặc định của Claude Code:
1. `docs/`
2. `src/`
3. `scripts/`
4. `tests/`

Claude chỉ coi đây là file và thư mục bình thường trong repo. Nếu muốn Claude hiểu:

1. `docs/` là nguồn chân lý
2. `src/` là code layer
3. `scripts/` là verification layer

thì bạn phải định nghĩa điều đó trong `CLAUDE.md`, rules, task packet, hoặc hook workflow.

---

## 3. Câu trả lời trực tiếp cho câu hỏi “khi tạo project mới tôi cần tạo gì?”

Tạo theo đúng thứ tự dưới đây.

---

## 4. Giai đoạn 1: Tạo lớp điều hướng tối thiểu

## Bước 1. Tạo root `CLAUDE.md`

Đây là file đầu tiên và quan trọng nhất.

### Mục đích
1. tóm tắt dự án
2. liệt kê module
3. chỉ đường cho Claude
4. chỉ rõ nguồn chân lý
5. cấm bịa nghiệp vụ
6. yêu cầu xác định đúng module trước khi sửa code

### Root `CLAUDE.md` nên có 6 phần

## 4.1 Project Context Summary
Ví dụ:

```md
# Project Context

Project: Payment Platform
Type: Multi-module backend system
Tech stack: Java Spring Boot, Maven multi-module, PostgreSQL, REST, SOAP integration

Main goals:
- manage invoice, payment, refund
- integrate with external payment providers
- keep module boundaries clean
```

## 4.2 Module Map Summary
Ví dụ:

```md
# Module Map

- auth: identity, login, token, access control
- billing: invoice, payment, refund
- order: order lifecycle, cart, checkout
- integration: third-party adapters
- common: shared dto, errors, utilities
```

## 4.3 Module Discovery Rule
Ví dụ:

```md
# Module Discovery Rule

When receiving a task:
1. extract business keywords
2. look up docs/02-architecture/02.glossary.md
3. identify primary module from glossary
4. read that module's CLAUDE.md
5. only then inspect code in src/
```

## 4.4 Anti-Hallucination Rule
Ví dụ:

```md
# Anti-Hallucination

Do not invent business behavior.

If behavior is unclear:
1. check glossary
2. check BPMN
3. check spec or contract
4. if still unclear, mark Assumption explicitly
```

## 4.5 Ground Truth Priority
Ví dụ:

```md
# Ground Truth Priority

1. docs/task-packets/current.md
2. docs/04-spec-test/*
3. docs/03-business-analysis/*
4. docs/02-architecture/*
5. src/<module>/CLAUDE.md
6. existing source code
```

## 4.6 Work Completion Rule
Ví dụ:

```md
# Before declaring done

1. confirm target module
2. list changed files
3. run relevant checks
4. explain risks
5. state whether architecture or spec changed
```

---

## Bước 2. Tạo `docs/02-architecture/02.glossary.md`

Đây là file thứ hai phải có.

### Mục đích
1. từ điển hoá thuật ngữ nghiệp vụ
2. gắn mỗi khái niệm với module chính
3. thêm synonym để Claude tìm đúng module nhanh hơn

### Mẫu

```md
# Glossary

## Invoice
Meaning: hóa đơn thanh toán
Primary module: billing
Related terms: payment, refund
Synonyms: bill, invoice record

## Login
Meaning: xác thực người dùng
Primary module: auth
Related terms: token, session, credential
Synonyms: sign in, authenticate

## Checkout
Meaning: xác nhận mua hàng
Primary module: order
Related terms: cart, place order, payment initiation
Synonyms: place order, finalize purchase
```

### Quy tắc viết glossary
1. mỗi term nên có đúng một `Primary module`
2. thêm `Synonyms` để Claude map keyword tốt hơn
3. nếu term liên quan nhiều module thì vẫn phải chọn một module chính và ghi thêm `Related modules`

---

## Bước 3. Tạo `docs/02-architecture/01.module-index.md`

### Mục đích
1. tạo bảng tra nhanh module
2. cho Claude biết module nào ở path nào
3. giảm thời gian scan repo

### Mẫu

```md
# Module Index

| Module | Purpose | Main keywords | Main paths |
|--------|---------|---------------|------------|
| auth | authentication and authorization | login, token, user, session | src/auth |
| billing | invoice and payment processing | invoice, payment, refund | src/billing |
| order | order lifecycle | cart, checkout, order | src/order |
| integration | third-party access | gateway, provider, adapter | src/integration |
| common | shared code | dto, util, error, common | src/common |
```

### Quy tắc viết module index
1. mỗi module phải có `Purpose`
2. mỗi module phải có `Main keywords`
3. mỗi module phải có `Main paths`
4. nếu module có alias thì thêm vào `Main keywords`

---

## 5. Giai đoạn 2: Tạo lớp module boundary

Sau khi đã có root `CLAUDE.md`, glossary và module index, bạn mới tạo `CLAUDE.md` cho từng module.

## Bước 4. Tạo `src/<module>/CLAUDE.md`

Ví dụ:
1. `src/auth/CLAUDE.md`
2. `src/billing/CLAUDE.md`
3. `src/order/CLAUDE.md`
4. `src/integration/CLAUDE.md`

## Mỗi module `CLAUDE.md` nên có 6 phần

### 5.1 Module purpose
```md
## Purpose
Handle invoice, payment, refund.
```

### 5.2 Owned concepts
```md
## Owned concepts
- Invoice
- Payment
- Refund
- Transaction
```

### 5.3 Not responsible for
```md
## Not responsible for
- user login
- order lifecycle
- UI rendering
```

### 5.4 Entry points
```md
## Entry points
- BillingController
- PaymentService
- RefundService
```

### 5.5 External integrations
```md
## External integrations
- payment gateway
- external billing provider
```

### 5.6 Local rules
```md
## Local rules
- do not implement order business logic here
- do not directly access auth storage
- payment errors must be translated to billing exceptions
```

## Mẫu hoàn chỉnh cho `src/billing/CLAUDE.md`

```md
# Module: billing

## Purpose
Handle invoice, payment, refund.

## Owned concepts
- Invoice
- Payment
- Refund
- Transaction

## Not responsible for
- user login
- order lifecycle
- UI rendering

## Entry points
- BillingController
- PaymentService
- RefundService

## External integrations
- payment gateway
- external billing provider

## Local rules
- do not implement order business logic here
- do not directly access auth storage
- payment errors must be translated to billing exceptions
```

---

## 6. Giai đoạn 3: Tạo `.claude/rules/`

Rules không nhằm thay thế `CLAUDE.md`. Rules dùng để chia nhỏ những nguyên tắc chung theo từng chủ đề.

Tạo trước 4 file này.

## 6.1 `.claude/rules/task-execution.md`

### Mục tiêu
1. luôn đọc task packet trước
2. luôn xác định module trước
3. luôn liệt kê assumptions nếu thiếu dữ liệu
4. luôn sửa theo phạm vi tối thiểu

### Mẫu

```md
# Task Execution Rule

Before editing code:
1. read docs/task-packets/current.md
2. extract business keywords
3. map keywords to glossary
4. identify target module
5. read target module CLAUDE.md

Implementation rules:
1. prefer minimal change
2. avoid cross-module refactor unless required
3. state assumptions explicitly if truth source is missing
```

## 6.2 `.claude/rules/truth-check.md`

### Mục tiêu
1. không khẳng định đúng nếu chưa đối chiếu spec
2. compile chưa đủ để gọi là done
3. phải có proof

### Mẫu

```md
# Truth Check Rule

Do not claim correctness based only on compilation.

Correctness requires at least:
1. alignment with task packet
2. alignment with glossary and business flow
3. relevant tests or checks
4. no unexplained cross-module drift

If truth source is missing, mark Assumption.
```

## 6.3 `.claude/rules/anti-drift.md`

### Mục tiêu
1. tránh pattern mới phá module
2. tránh refactor lan man
3. tránh chạm module không đúng nhiệm vụ

### Mẫu

```md
# Anti-Drift Rule

1. Do not create a second pattern if the module already has a stable pattern.
2. Do not refactor unrelated modules unless required by the task.
3. If a change crosses module boundary, explain why.
4. Keep business logic in its owning module.
```

## 6.4 `.claude/rules/library-selection.md`

### Mục tiêu
1. ưu tiên library chuẩn
2. tránh tự code lại thứ library đã xử lý tốt
3. nếu thêm library mới phải giải thích

### Mẫu

```md
# Library Selection Rule

1. Prefer approved or existing libraries in the project.
2. Do not reimplement a solved problem without strong reason.
3. If proposing a new library:
   - explain why
   - explain alternatives
   - explain module impact
```

---

## 7. Giai đoạn 4: Tạo tầng chân lý kiến trúc và nghiệp vụ

Sau khi có lớp điều hướng và module boundary, bạn tạo tiếp các tài liệu chân lý.

## 7.1 `docs/02-architecture/04.architecture-c4.md`
Mục đích:
1. cho Claude hiểu module và quan hệ giữa module
2. cho Claude thấy luồng tổng thể

## 7.2 `docs/02-architecture/06.adr.md`
Mục đích:
1. ghi lại quyết định kiến trúc
2. để Claude không đề xuất solution trái định hướng

## 7.3 `docs/03-business-analysis/09.bpmn.md`
Mục đích:
1. mô tả flow nghiệp vụ
2. giúp Claude hiểu trình tự bước nghiệp vụ

## 7.4 `docs/04-spec-test/*`
Mục đích:
1. mô tả contract
2. mô tả request response
3. mô tả validation
4. mô tả acceptance

---

## 8. Giai đoạn 5: Tạo skills

Skill là runbook tái sử dụng. Chỉ nên tạo các skill bạn sẽ dùng lặp lại.

Tạo trước 4 skill.

## 8.1 `.claude/skills/find-module/SKILL.md`

### Mục tiêu
1. trích business keyword từ task
2. tra glossary
3. map sang module
4. xuất module chính và module liên quan

### Mẫu

```md
# Find Module

Goal:
Find the correct module before code inspection.

Steps:
1. Extract business keywords from the task.
2. Look up docs/02-architecture/02.glossary.md.
3. Look up docs/02-architecture/01.module-index.md.
4. Identify:
   - primary module
   - related modules
   - paths to inspect first

Output:
- Primary module:
- Related modules:
- Reason:
- First files to read:
```

## 8.2 `.claude/skills/plan-task/SKILL.md`

### Mục tiêu
1. lập kế hoạch xử lý task
2. chỉ ra file cần đọc
3. chỉ ra file có thể sửa
4. chỉ ra checks cần chạy

### Mẫu

```md
# Plan Task

Steps:
1. Read docs/task-packets/current.md
2. Identify target module
3. List first files to inspect
4. Propose minimal change plan
5. List tests or checks to run

Output:
- Task summary
- Target module
- Files to inspect first
- Files likely to change
- Checks to run
- Risks
```

## 8.3 `.claude/skills/implement-from-spec/SKILL.md`

### Mục tiêu
1. implement dựa trên spec
2. không đi quá phạm vi
3. thêm test phù hợp

### Mẫu

```md
# Implement From Spec

Steps:
1. Read task packet
2. Read glossary and module CLAUDE.md
3. Read relevant spec or contract
4. Inspect code in target module
5. Implement minimal change
6. Add or update relevant tests
7. Summarize proof
```

## 8.4 `.claude/skills/preflight-check/SKILL.md`

### Mục tiêu
1. trước khi báo done
2. tự kiểm xem đã đủ proof chưa

### Mẫu

```md
# Preflight Check

Checklist:
1. Did I confirm the correct module?
2. Did I use the glossary?
3. Did I read the module CLAUDE.md?
4. Did I stay within scope?
5. Did I update tests if needed?
6. Did I list changed files, risks, and assumptions?
```

---

## 9. Giai đoạn 6: Tạo hooks và scripts kiểm tra

Bước này biến hệ thống thành có cưỡng chế.

## Bước 9.1 Tạo `.claude/settings.json`

Mục tiêu:
1. sau khi sửa file thì chạy kiểm tra nhanh
2. trước khi kết thúc task thì chạy truth check
3. nếu sửa sai module hoặc vượt scope thì cảnh báo hoặc fail

Ví dụ khái niệm:

```json
{
  "hooks": {
    "post-edit": [
      "python scripts/check_module_scope.py"
    ],
    "pre-submit": [
      "python scripts/check_truth.py"
    ]
  }
}
```

Lưu ý:
1. tên hook event cụ thể có thể thay đổi theo version của Claude Code
2. bạn nên kiểm tra docs hiện hành khi triển khai thật

## Bước 9.2 Tạo `scripts/check_task_packet.py`

### Mục tiêu
1. kiểm task packet có tồn tại không
2. có DoD không
3. có acceptance không

### Logic kiểm đề xuất
1. fail nếu thiếu `docs/task-packets/current.md`
2. fail nếu thiếu `Definition of Done`
3. warn nếu thiếu `Acceptance Tests`

## Bước 9.3 Tạo `scripts/check_truth.py`

### Mục tiêu
1. kiểm đã dùng truth source chưa
2. kiểm behavior mới có test chưa
3. kiểm có thay đổi contract mà quên update spec không

### Logic kiểm đề xuất
1. warn nếu task summary không nêu truth source
2. fail nếu sửa public contract nhưng không đụng `docs/04-spec-test/*`
3. warn nếu sửa behavior mà không đụng test

## Bước 9.4 Tạo `scripts/check_module_scope.py`

### Mục tiêu
1. kiểm task thuộc module nào
2. kiểm file sửa nằm ở module nào
3. phát hiện sửa lệch vùng

### Logic kiểm đề xuất
1. xác định `target module`
2. liệt kê các file changed
3. nếu file changed nằm ngoài target module thì warn hoặc fail tùy rule

---

## 10. Claude thực tế sẽ nhận thông tin và định vị module như thế nào

Đây là luồng vận hành chuẩn.

## Ví dụ task
“Sửa logic refund bị sai số tiền”

## Bước 1. Claude đọc root `CLAUDE.md`
Claude lấy được:
1. đây là project gì
2. phải tìm module trước
3. phải tra glossary
4. không được bịa nghiệp vụ

## Bước 2. Claude tách keyword từ task
Ví dụ:
1. refund
2. amount
3. payment

## Bước 3. Claude tra `docs/02-architecture/02.glossary.md`
Claude thấy:
1. refund thuộc module `billing`
2. payment cũng liên quan `billing`

## Bước 4. Claude tra `docs/02-architecture/01.module-index.md`
Claude thấy:
1. module chính là `billing`
2. path chính là `src/billing`

## Bước 5. Claude đọc `src/billing/CLAUDE.md`
Claude hiểu:
1. billing sở hữu refund
2. billing không nên xử lý order lifecycle
3. billing có entry points nào

## Bước 6. Claude mới bắt đầu đọc source code trong `src/billing`
Claude đọc:
1. controller
2. service
3. test
4. integration liên quan

## Bước 7. Claude dùng skill
Claude hoặc bạn có thể dùng:
1. `find-module`
2. `plan-task`
3. `implement-from-spec`

## Bước 8. Claude sửa code
Claude nên sửa theo nguyên tắc:
1. minimal change
2. đúng module
3. có test phù hợp

## Bước 9. Hooks và scripts chạy
Ví dụ:
1. `check_module_scope.py`
2. `check_truth.py`

## Bước 10. Claude mới được phép báo done
Summary nên có:
1. target module
2. files changed
3. checks run
4. risks
5. assumptions

---

## 11. Cách kết hợp rules, skills, hooks và MCP

## 11.1 Rule
Rule dùng để nói:
1. phải làm thế nào
2. phải đọc gì trước
3. cấm gì

Rule là luật.

## 11.2 Skill
Skill dùng để nói:
1. khi gặp tác vụ loại này thì đi theo runbook nào

Skill là quy trình mẫu.

## 11.3 Hook
Hook dùng để nói:
1. trước hoặc sau một hành động thì chạy kiểm tra gì

Hook là cảnh sát.

## 11.4 MCP
MCP dùng để nói:
1. Claude có thể lấy thêm dữ liệu ở đâu
2. Claude có thêm công cụ gì ngoài repo

Ví dụ:
1. docs nội bộ
2. issue tracker
3. DB schema explorer
4. internal API docs
5. search server

## 11.5 Chuỗi kết hợp đúng
1. `CLAUDE.md` chỉ hướng
2. `rules` giữ hành vi đúng
3. `skills` cung cấp runbook
4. `MCP` cung cấp dữ liệu ngoài repo nếu cần
5. `hooks` bắt Claude tự kiểm trước khi kết thúc

---

## 12. Checklist tuần tự khi khởi tạo project mới

## Giai đoạn 1. Tạo lớp điều hướng
1. `CLAUDE.md`
2. `docs/02-architecture/02.glossary.md`
3. `docs/02-architecture/01.module-index.md`

## Giai đoạn 2. Tạo lớp module
4. `src/auth/CLAUDE.md`
5. `src/billing/CLAUDE.md`
6. `src/order/CLAUDE.md`
7. `src/integration/CLAUDE.md`

## Giai đoạn 3. Tạo luật chi tiết
8. `.claude/rules/task-execution.md`
9. `.claude/rules/truth-check.md`
10. `.claude/rules/anti-drift.md`
11. `.claude/rules/library-selection.md`

## Giai đoạn 4. Tạo tầng chân lý
12. `docs/02-architecture/04.architecture-c4.md`
13. `docs/02-architecture/06.adr.md`
14. `docs/03-business-analysis/09.bpmn.md`
15. `docs/04-spec-test/...`

## Giai đoạn 5. Tạo runbook
16. `.claude/skills/find-module/SKILL.md`
17. `.claude/skills/plan-task/SKILL.md`
18. `.claude/skills/implement-from-spec/SKILL.md`
19. `.claude/skills/preflight-check/SKILL.md`

## Giai đoạn 6. Tạo lớp cưỡng chế
20. `.claude/settings.json`
21. `scripts/check_task_packet.py`
22. `scripts/check_truth.py`
23. `scripts/check_module_scope.py`

---

## 13. Bộ tối giản để bắt đầu nhanh

Nếu bạn chưa muốn tạo hết toàn bộ hệ thống, chỉ cần tạo trước 8 thứ sau:

1. `CLAUDE.md`
2. `docs/02-architecture/02.glossary.md`
3. `docs/02-architecture/01.module-index.md`
4. `src/<module>/CLAUDE.md` cho từng module chính
5. `.claude/rules/task-execution.md`
6. `.claude/rules/truth-check.md`
7. `.claude/skills/find-module/SKILL.md`
8. `.claude/settings.json`

Bộ này đã đủ để:
1. giúp Claude tìm module đúng hơn
2. giảm bịa nghiệp vụ
3. bắt đầu có kiểm tra nền

---

## 14. Mẫu cấu trúc repo hoàn chỉnh

```text
repo/
├─ CLAUDE.md
├─ .claude/
│  ├─ settings.json
│  ├─ rules/
│  │  ├─ task-execution.md
│  │  ├─ truth-check.md
│  │  ├─ anti-drift.md
│  │  └─ library-selection.md
│  └─ skills/
│     ├─ find-module/
│     │  └─ SKILL.md
│     ├─ plan-task/
│     │  └─ SKILL.md
│     ├─ implement-from-spec/
│     │  └─ SKILL.md
│     └─ preflight-check/
│        └─ SKILL.md
├─ docs/
│  └─ 02-architecture/
│     ├─ 01.module-index.md
│     ├─ 02.glossary.md
│     ├─ 04.architecture-c4.md
│     └─ 06.adr.md
├─ docs/
│  ├─ 03-business-analysis/
│  │  └─ 09.bpmn.md
│  └─ 04-spec-test/
├─ src/
│  ├─ auth/
│  │  └─ CLAUDE.md
│  ├─ billing/
│  │  └─ CLAUDE.md
│  ├─ order/
│  │  └─ CLAUDE.md
│  └─ integration/
│     └─ CLAUDE.md
├─ scripts/
│  ├─ check_task_packet.py
│  ├─ check_truth.py
│  └─ check_module_scope.py
└─ tests/
```

---

## 15. Câu kết luận dễ nhớ nhất

Claude nên làm việc theo chuỗi này:

**task → glossary → module-index → module CLAUDE.md → source code → skill runbook → hook verify**

Và khi tạo project mới, hãy tạo theo thứ tự:

**root CLAUDE.md → glossary → module-index → module CLAUDE.md → rules → skills → hooks và scripts**

Nếu làm theo đúng chuỗi này, Claude sẽ:
1. định vị đúng module nhanh hơn
2. ít drift hơn
3. ít bịa nghiệp vụ hơn
4. dễ verify hơn

---

## 16. Ghi chú triển khai thực tế

1. `CLAUDE.md` nên ngắn, rõ, không nhồi mọi chi tiết
2. các quy tắc chi tiết nên tách sang `.claude/rules/`
3. các quy trình nhiều bước nên tách thành skill
4. `docs/` không tự động là truth source nếu bạn không khai báo
5. `scripts/` không tự động là enforcement nếu không gắn vào hooks hoặc CI
6. glossary và module index là hai file có lợi suất cao nhất cho hệ multi-module
7. module `CLAUDE.md` là vũ khí mạnh nhất để chống nhầm boundary

---

## 17. Tài liệu tham khảo chính thức

1. Claude Code memory và project instructions:
   https://docs.anthropic.com/en/docs/claude-code/memory

2. Claude Code skills:
   https://docs.anthropic.com/en/docs/claude-code/skills

3. Claude Code hooks:
   https://docs.anthropic.com/en/docs/claude-code/hooks-guide

4. Claude Code overview:
   https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview

## Related




## References

- [manifest.yaml](../docs/grounding/manifest.yaml)
- [manifest.md](../docs/grounding/manifest.md)
- [AGENTS.md](../AGENTS.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
