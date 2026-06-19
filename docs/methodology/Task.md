---
id: methodology-task-workspace
title: Task Workspace Lifecycle
type: methodology
category: methodology
status: published
owner: governance
domain: agentic-sdlc
project: ftransform
workspace: docs
created: 2026-05-13
updated: 2026-05-14
sensitivity: internal
truth_level: official
source_policy: source_of_truth
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
  - docs/grounding/prompts.md
  - docs/grounding/skills/8.archive-current-task.md
  - docs/grounding/skills/9.create-current-task.md
  - docs/09-runbook/17.dor.md
  - docs/09-runbook/18.dod.md
  - docs/06-quality-assurance/14.qa-matrix.md
related:
  - docs/methodology/agent_request_lifecycle_kms_sdd_grounding.md
  - docs/01-requirements/00.requirement.md
  - docs/03-business-analysis/09.bpmn.md
  - docs/09-runbook/17.dor.md
  - docs/09-runbook/18.dod.md
  - docs/06-quality-assurance/14.qa-matrix.md
  - llm-wiki/tasks/README.md
  - llm-wiki/tasks/current/TASK.md
tags:
  - sdd/methodology
  - sdd/reusable
  - sdd/generic
---

# Task Workspace Lifecycle

## Mục Đích

Tài liệu này là hướng dẫn chính thức cho việc tạo, sử dụng, truy vết và archive task workspace trong `llm-wiki/tasks/`.

`llm-wiki/tasks/` là workspace memory, không phải source of truth. Source of truth chính thức vẫn nằm trong `docs/`, executable specs, contracts, ADR, QA matrix, runbook, và explicit user decision.

## Context

Tài liệu này điều khiển cách agent tạo task workspace, chọn task profile, ghi evidence và đề xuất promotion. Nó không thay thế requirement, architecture, contract, QA matrix, runbook hoặc quyết định của user.

## Quy Ước Thư Mục

```text
llm-wiki/tasks/current/
llm-wiki/tasks/archive/YYYYMMDD_NNN_short-name/
```

`current` là workspace của task đang làm. Các thư mục có ngày trong `archive/` là lịch sử task đã archive.

`llm-wiki/tasks/archive/` là kho tư liệu lịch sử để tìm kiếm khi cần thiết. Agent không scan toàn bộ thư mục này mặc định cho mọi task; chỉ search khi task mới có dấu hiệu liên quan đến việc cũ, user yêu cầu truy vết, hoặc cần bằng chứng lịch sử để hiểu quyết định trước đó.

## Cách Dùng

1. Khi nhận task mới, dùng Skill 9 với mô tả task dự định làm.
2. Skill 9 kiểm tra `current/`; nếu đang có task hiện tại thì dùng Skill 8 để archive trước.
3. Skill 9 phân tích mục tiêu task, task type, artifact layer, verification expectation và lịch sử task liên quan khi cần.
4. Skill 9 tạo nội dung phù hợp trong `current/`; không copy form trống từ template.
5. Trong quá trình làm task, ghi quyết định đã chốt vào `current/decision-log.md`.
6. Nếu cần promote sang official docs, ghi target trong `current/promotion.md`.
7. Sau khi task xong, nếu `current` có nội dung hữu ích, dùng Skill 8 để archive sang `archive/YYYYMMDD_NNN_short-name` rồi clean `current`.
8. Task tiếp theo lại bắt đầu bằng Skill 9.

## Vai Trò File Chính

| File | Vai trò |
|---|---|
| `TASK.md` | Tổng hợp task workspace và trạng thái hiện tại |
| `raw.md` | Lưu input nguyên bản trước khi phân tích |
| `intake.md` | Chuyển input thành task brief có scope, risk, artifact layer |
| `decision-log.md` | Ghi các quyết định đã chốt và source của quyết định |
| `promotion.md` | Quyết định có promote sang `docs/` hay không |
| `verification-notes.md` | Lưu bằng chứng verify, warning, error, verdict |

## Hướng Dẫn Cho Skill 9

Skill 9 dùng tài liệu này làm hướng dẫn tạo task mới. Nó nhận mô tả task từ user, phân tích mục tiêu task, rồi tạo nội dung phù hợp trong `llm-wiki/tasks/current/`.

Hướng dẫn này không phải requirement của sản phẩm và không được dùng để code trực tiếp. Nó chỉ mô tả cách agent nên tạo các file workspace, phân tích task, ghi quyết định, verification và promotion trước khi nội dung nào đó được chốt sang artifact chính thức.

### Khi Nào Dùng

Dùng hướng dẫn này khi:

1. Bắt đầu một task mới.
2. Agent được gọi bằng Skill 9 với mô tả task dự định làm.
3. Cần phân tích task trước khi promote sang `docs/`.
4. Cần tạo `current` có nội dung phù hợp thay vì copy form trống.
5. Cần tham khảo lịch sử task cũ để truy vết quyết định hoặc quá trình xây dựng hệ thống.

### Cấu Trúc Một Task Workspace

Skill tạo task mới quyết định file nào cần tạo dựa trên loại task. Không bắt buộc task nào cũng cần đủ mọi file.

| File | Vai trò | Khi nào tạo | Nội dung tối thiểu |
|---|---|---|---|
| `TASK.md` | Bản tổng hợp task workspace | Luôn tạo | Mục tiêu task, task type, artifact layer, ready status, nguồn cần đọc, next skill |
| `raw.md` | Lưu input nguyên bản | Khi task bắt đầu từ yêu cầu, ticket, email, log, hoặc chat có nội dung cần giữ nguyên văn | Raw text, ngày nhận, nguồn, cảnh báo PII/secret nếu có |
| `intake.md` | Chuyển raw input thành task brief có cấu trúc | Luôn tạo cho task không tầm thường | Scope, risk, impacted model, open questions, verification expectation |
| `decision-log.md` | Ghi các quyết định đã chốt | Tạo khi có hoặc dự kiến cần decision | Decision, source, impact, promotion needed |
| `promotion.md` | Quyết định có promote sang `docs/` không | Tạo khi task có khả năng đổi source of truth | Target artifact layer, target path, manifest/sources impact |
| `verification-notes.md` | Ghi bằng chứng kiểm tra | Tạo khi task có code/docs/verification claim | Command/check, source command, exit code, warnings/errors, verdict |
| `analysis.md` | Phân tích tự do, tùy chọn | Khi task phức tạp cần reasoning riêng | Findings, trade-off, blockers, source references |
| `clarification-log.md` | Hỏi đáp làm rõ, tùy chọn | Khi request mơ hồ | Câu hỏi, câu trả lời, status, decision sinh ra |
| `contradictions.md` | Mâu thuẫn trong task, tùy chọn | Khi source trong task mâu thuẫn nhau | Source A, Source B, conflict, impact, proposed resolution |

### Cách Skill 9 Tạo Task Mới

1. Nhận mô tả task từ user.
2. Kiểm tra `llm-wiki/tasks/current/`; nếu có nội dung hữu ích, chạy Skill 8 để archive trước.
3. Tìm lịch sử task liên quan trong `llm-wiki/tasks/archive/YYYYMMDD_NNN_short-name/` khi task có dấu hiệu nối tiếp việc cũ.
4. Phân tích mục tiêu task, task type, risk, impacted domain/runtime model, artifact layer và verification expectation.
5. Tạo hoặc làm mới `llm-wiki/tasks/current/`.
6. Sinh nội dung phù hợp cho `TASK.md`, `raw.md`, `intake.md` và các file phụ cần thiết.
7. Ghi rõ task history nào đã được tham khảo, nếu có.
8. Không promote sang `docs/` trong bước tạo task; promotion chỉ diễn ra khi decision đã đủ rõ và scope cho phép.

### Task Profile Theo Loại Công Việc

Skill 9 phải chọn task profile theo mục tiêu thật của request. Một task có thể dùng nhiều profile, nhưng `TASK.md` cần ghi profile chính để agent biết cần đọc nguồn nào, tạo file nào và verify bằng loại evidence nào.

#### 1. Task Phân Tích Yêu Cầu

Dùng khi user đưa ý tưởng, mô tả nghiệp vụ, vấn đề cần giải quyết, yêu cầu còn mơ hồ, hoặc muốn biến input thành requirement/use case/spec trước khi thiết kế hoặc code.

| Trường | Chỉ dẫn |
|---|---|
| Task type | `requirement-analysis` |
| Artifact layer | Requirement, use case, glossary, BPMN, acceptance criteria |
| Nguồn cần đọc | `docs/01-requirements/`, `docs/02-architecture/02.glossary.md`, `docs/03-business-analysis/09.bpmn.md`, `docs/methodology/d_2.idea_to_spec_before_design.md`, task history liên quan nếu có |
| File workspace nên tạo | `raw.md`, `intake.md`, `analysis.md`, `clarification-log.md`, `decision-log.md`, `promotion.md` |
| Output mong đợi | Problem statement, scope, actor/stakeholder, use cases, acceptance criteria sơ bộ, assumptions, open questions, risk |
| Verification expectation | Kiểm tra trace requirement -> use case -> acceptance criteria; ghi rõ phần nào thiếu evidence hoặc cần human confirm |

`intake.md` cho loại task này nên có các mục:

```text
Problem statement:
User goal:
Stakeholders or actors:
In scope:
Out of scope:
Assumptions:
Open questions:
Acceptance criteria draft:
Candidate docs to update:
Ready for design:
```

Nếu requirement chưa đủ rõ, Skill 9 không được tự chốt business rule. Ghi `BLOCKED` hoặc `NEEDS CLARIFICATION` trong `TASK.md`, đồng thời lưu câu hỏi trong `clarification-log.md`.

#### 2. Task Sửa Đổi Nghiệp Vụ

Dùng khi task thay đổi rule, workflow, trạng thái, tính toán, policy, mapping dữ liệu, output người dùng thấy, hoặc hành vi runtime có ý nghĩa nghiệp vụ.

| Trường | Chỉ dẫn |
|---|---|
| Task type | `business-behavior-change` |
| Artifact layer | Requirement, BPMN/business flow, contract/spec, QA matrix, implementation evidence |
| Nguồn cần đọc | Requirement/use case liên quan, glossary, BPMN hoặc business flow, contract/spec liên quan, QA matrix, DoR/DoD, code boundary liên quan |
| File workspace nên tạo | `raw.md`, `intake.md`, `analysis.md`, `decision-log.md`, `contradictions.md` nếu có mâu thuẫn, `promotion.md`, `verification-notes.md` |
| Output mong đợi | Current behavior, expected behavior, impacted business rule, impacted files, compatibility risk, acceptance mapping, verification plan |
| Verification expectation | Có evidence cho business behavior mới: test, acceptance check, dry-run, log, output artifact, hoặc ghi rõ lý do chưa verify được |

`intake.md` cho loại task này nên có các mục:

```text
Current behavior:
Requested business change:
Expected behavior:
Impacted business flow:
Impacted contract or output:
Files in scope:
Files out of scope:
Backward compatibility:
Acceptance criteria:
Verification plan:
Human decision needed:
```

Quy tắc bắt buộc:

1. Không sửa code trước khi xác định source-of-truth hoặc gap của rule nghiệp vụ.
2. Nếu docs và code lệch nhau, ghi contradiction; không tự chọn bên thắng nếu chưa có user decision hoặc artifact chính thức.
3. Nếu thay đổi trở thành knowledge chính thức, `promotion.md` phải chỉ rõ cần update requirement, BPMN, contract, QA matrix, runbook hay grounding manifest.
4. Final task evidence phải phân biệt `VERIFIED`, `PARTIALLY VERIFIED`, `BLOCKED`, hoặc `NEEDS HUMAN REVIEW`.

#### 3. Task Package Và Release

Dùng khi task chuẩn bị bàn giao, đóng gói, build artifact, release note, cài đặt, upgrade, deployment, smoke test, rollback, handover, hoặc gom evidence cho nghiệm thu.

| Trường | Chỉ dẫn |
|---|---|
| Task type | `package-release` |
| Artifact layer | DoD, runbook, release, QA matrix, review checklist, evidence pack |
| Nguồn cần đọc | `docs/09-runbook/17.dor.md`, `docs/09-runbook/18.dod.md`, `docs/06-quality-assurance/14.qa-matrix.md`, review checklist, install/run docs, CI/build config, prior verification notes |
| File workspace nên tạo | `raw.md`, `intake.md`, `analysis.md`, `decision-log.md`, `verification-notes.md`, `promotion.md`, tùy chọn `release-evidence.md` |
| Output mong đợi | Package scope, version/build ID, install guide impact, runbook steps, test package, evidence pack, known issues, rollback note, release readiness verdict |
| Verification expectation | Build/package command, install/smoke test command, artifact path/checksum nếu có, test result, warnings/errors, manual review items, release approval gap |

`intake.md` cho loại task này nên có các mục:

```text
Release objective:
Package scope:
Version or build identifier:
Artifacts to produce:
Install or upgrade guide needed:
Runbook updates needed:
Tests to package:
Evidence to collect:
Smoke test plan:
Rollback plan:
Known issues:
Release approval owner:
Ready for release:
```

Task package và release phải bổ sung hoặc xác nhận các nhóm nội dung sau khi chúng nằm trong scope:

1. Hướng dẫn sử dụng: command, cấu hình, input/output, ví dụ tối thiểu, lỗi thường gặp.
2. Runbook cài đặt: prerequisite, install steps, environment variables, start/stop, health check, rollback.
3. Đóng gói test: test list, command, fixture/sample data, expected output, cách đọc pass/fail.
4. Evidence pack: exact command, exit code, log warning/error, artifact path, checksum nếu cần, screenshot/output nếu phù hợp, known issues.
5. Handover note: version, scope, limitation, owner, approval gap, next action.

Nếu thiếu command hoặc môi trường để verify package, Skill 9 phải ghi `PARTIALLY VERIFIED` hoặc `BLOCKED`, không được ghi release-ready chỉ dựa trên mô tả.

### Quy Hoạch Promotion Sang `docs/`

`promotion.md` không chỉ ghi có promote hay không. Với ba task profile trên, nó phải đề xuất phương án quy hoạch tài liệu sang kho `docs/` để human chọn hoặc review. Agent không tự promote mọi ghi chú workspace; chỉ promote phần đã đủ evidence hoặc được user chốt.

#### Nguyên Tắc Promotion Chung

Áp dụng cho mọi loại task, không chỉ ba profile bên dưới.

Promotion là quá trình chuyển tri thức từ workspace (`llm-wiki/tasks/current/`) sang source-of-truth hoặc tài liệu chính thức trong `docs/`, `tests/`, `specs/`, runbook, QA matrix, hoặc artifact release. Promotion không phải là copy toàn bộ task note.

Một nội dung chỉ nên promote khi thỏa ít nhất một điều kiện:

1. Nó là quyết định được user hoặc source chính thức chốt.
2. Nó thay đổi behavior, contract, architecture, workflow, setup, verification, hoặc release process.
3. Nó là assumption/open question cần được quản lý chính thức.
4. Nó là evidence cần dùng để nghiệm thu, audit, release, hoặc debug sau này.
5. Nó sửa lỗi hoặc bổ sung source-of-truth đang thiếu, sai, hoặc drift so với implementation/evidence.

Không promote khi:

1. Nội dung chỉ là reasoning tạm thời của agent.
2. Nội dung là log thô, output dài, hoặc scratch note không còn giá trị.
3. Nội dung chưa có evidence và chưa được human chốt.
4. Nội dung trùng lặp với docs hiện có mà không thêm quyết định, trace, hoặc update mới.
5. Nội dung có secret, PII, dữ liệu nhạy cảm, hoặc artifact không nên lưu trong repo.

#### Promotion Decision Tree

Khi viết `promotion.md`, agent dùng decision tree sau:

| Câu hỏi | Nếu có | Target gợi ý |
|---|---|---|
| Có thay đổi mục tiêu, scope, yêu cầu, AC? | Promote requirement/spec | `docs/01-requirements/`, `tests/features/`, `tests/bdd/` |
| Có thay đổi thuật ngữ, actor, bounded context? | Promote glossary/context | `docs/02-architecture/02.glossary.md`, `docs/03-business-analysis/02.glossary.md` |
| Có thay đổi kiến trúc, boundary, dependency, trade-off? | Promote architecture/ADR | `docs/02-architecture/`, `docs/02-architecture/adr/` |
| Có thay đổi workflow nghiệp vụ hoặc operational flow? | Promote BPMN/runbook | `docs/03-business-analysis/`, `docs/09-runbook/` |
| Có thay đổi API/application/schema/UI/output contract? | Promote contract | `docs/04-spec-test/`, `specs/` |
| Có thay đổi test strategy, gate, known caveat? | Promote QA/review | `docs/06-quality-assurance/`, `docs/07-ci-cd-review/` |
| Có thay đổi setup, deploy, run, rollback? | Promote runbook | `docs/09-runbook/` |
| Có release/build/handover evidence? | Promote release evidence | `docs/10-release/` hoặc artifact repository |
| Có observation hữu ích nhưng chưa official? | Keep workspace | `llm-wiki/` |

#### Mức Promotion

Không phải task nào cũng cần promotion đầy đủ. Chọn mức nhỏ nhất đủ kiểm soát drift.

| Mức | Dùng khi | Kết quả |
|---|---|---|
| No promotion | Task không tạo knowledge bền vững | Ghi lý do trong `promotion.md` |
| Workspace only | Có observation tạm thời, chưa đủ official | Giữ trong `llm-wiki/tasks/current/` hoặc archive |
| Link existing docs | Docs đã đúng, chỉ cần chỉ ra source | `promotion.md` link tới docs hiện có |
| Patch existing docs | Source-of-truth cần cập nhật nhỏ | Sửa file hiện có và update related links |
| Create draft doc | Knowledge mới nhưng chưa reviewed | Tạo doc `status: draft` đúng artifact layer |
| Promote official update | User/source đã chốt và evidence đủ | Cập nhật docs/spec/tests/runbook/QA tương ứng |

#### Checklist Cho `promotion.md`

Mọi `promotion.md` nên trả lời tối thiểu:

```text
Promotion needed:
Promotion level:
Reason:
Promote from:
Promote to:
Artifact layer:
Source evidence:
Related links to add:
Docs needing update:
Tests/specs needing update:
Grounding/manifest impact:
Content not promoted:
Reason not promoted:
Human approval needed:
Sync status:
```

`Sync status` nên dùng một trong các giá trị:

| Status | Ý nghĩa |
|---|---|
| `NO_PROMOTION_NEEDED` | Task không tạo durable knowledge. |
| `WORKSPACE_ONLY` | Chỉ giữ note trong workspace/archive. |
| `SYNCED` | Docs/spec/tests/runbook đã được cập nhật phù hợp. |
| `PARTIALLY_SYNCED` | Một phần đã promote, còn gap rõ ràng. |
| `SYNC_NEEDED` | Có thay đổi cần promote nhưng chưa làm trong scope task. |
| `BLOCKED` | Không thể promote vì thiếu quyết định, evidence, hoặc source conflict. |

#### Quy Tắc An Toàn Khi Promote

1. Promote theo artifact layer, không tạo doc mới nếu file canonical đã tồn tại.
2. Không biến `llm-wiki` thành source of truth; dùng nó làm nguồn quan sát và trace.
3. Không promote business rule, security rule, release decision, hoặc architecture decision nếu chưa có source/user confirmation.
4. Khi sửa Markdown trong `docs/`, giữ frontmatter, một H1, Purpose/Context, Related/References và link spine.
5. Nếu promote làm thay đổi source catalog, prompt governance, hoặc grounding manifest, ghi rõ cần cập nhật `docs/grounding/manifest.*`, `sources.md`, hoặc `prompts.md`.
6. Nếu không đủ scope để promote ngay, final report phải nêu `SYNC_NEEDED` hoặc `PARTIALLY_SYNCED`.

#### Promotion Cho Task Phân Tích Yêu Cầu

Mục tiêu là chuyển input mơ hồ thành requirement/spec có trace.

| Phương án | Khi dùng | Target docs |
|---|---|---|
| Promote tối thiểu | Mới chỉ đủ problem, scope, open questions | `docs/01-requirements/00.requirement.md`, `docs/01-requirements/01.use-cases.md` |
| Promote theo flow | Đã rõ actor, happy path, error path, decision point | `docs/03-business-analysis/09.bpmn.md`, có link ngược về requirement |
| Promote thuật ngữ | Có thuật ngữ domain mới hoặc định nghĩa cần thống nhất | `docs/02-architecture/02.glossary.md` hoặc `docs/03-business-analysis/02.glossary.md` |
| Promote acceptance | Có AC đủ kiểm | `tests/features/` hoặc `tests/bdd/` khi có test framework; nếu chưa có thì ghi AC trong requirement/use case |

`promotion.md` nên ghi:

```text
Recommended promotion option:
Docs to update:
New docs needed:
Trace links to add:
Unpromoted workspace notes:
Reason not promoted:
```

#### Promotion Cho Task Sửa Đổi Nghiệp Vụ

Mục tiêu là đảm bảo business change không chỉ nằm trong code hoặc task note.

| Phương án | Khi dùng | Target docs |
|---|---|---|
| Promote rule change | Rule nghiệp vụ được user/source chốt | Requirement, use case, glossary, hoặc business analysis doc liên quan |
| Promote flow change | Thay đổi thứ tự bước, branch, trạng thái, exception path | `docs/03-business-analysis/09.bpmn.md` |
| Promote contract change | Output/API/application/schema thay đổi | `docs/04-spec-test/`, `specs/`, hoặc contract hiện có |
| Promote QA change | Verification mới, regression case mới, caveat mới | `docs/06-quality-assurance/14.qa-matrix.md`, review checklist, test docs |
| Promote operational impact | Thay đổi cách vận hành, cấu hình, migration, rollback | `docs/09-runbook/` |

`promotion.md` cho loại task này phải nêu rõ:

```text
Business rule source:
Docs/code drift found:
Promotion option:
Artifacts to update together:
Acceptance evidence:
Manifest/sources impact:
Human approval needed:
```

Nếu chỉ sửa code mà không update docs dù behavior chính thức đổi, task phải ghi `PARTIALLY_SYNCED` hoặc `SYNC NEEDED`.

#### Promotion Cho Task Package Và Release

Mục tiêu là gom tài liệu bàn giao, cài đặt, test và evidence thành bộ release có thể review lại.

| Phương án | Khi dùng | Target docs |
|---|---|---|
| Promote usage guide | Người dùng/dev cần biết cách chạy hoặc dùng artifact | `README.md`, module README, hoặc `docs/09-runbook/` |
| Promote install runbook | Có bước cài đặt, upgrade, config, health check, rollback | `docs/09-runbook/` |
| Promote release note | Có version/build, scope, known issues, compatibility | `docs/10-release/` |
| Promote evidence pack | Cần lưu lệnh build/test/smoke, exit code, artifact path, checksum | `docs/10-release/` hoặc release evidence note được link từ DoD |
| Promote QA gate | Test suite, smoke test, hoặc release gate thay đổi | `docs/06-quality-assurance/14.qa-matrix.md`, `docs/09-runbook/18.dod.md` |

`promotion.md` cho package/release nên có:

```text
Release docs plan:
Usage guide target:
Install runbook target:
Test package target:
Evidence pack target:
Release note target:
DoD/QA update needed:
Approval and handover owner:
```

Nếu project chưa có file release chính thức, agent nên đề xuất tạo draft trong `docs/10-release/` thay vì nhét toàn bộ evidence vào `llm-wiki/`. Workspace chỉ giữ working notes; release evidence dùng để nghiệm thu phải có đường dẫn rõ sang docs hoặc artifact repository.

### Cách Agent Tham Khảo Lịch Sử Task

Khi tạo task mới, agent nên search task history nếu:

1. Task nhắc tới feature/module đã từng làm.
2. Task tiếp nối bug, decision, refactor hoặc verification trước đó.
3. User hỏi “tiếp tục”, “như lần trước”, “sửa tiếp”, “điều chỉnh lại”.
4. Cần truy vết vì sao một artifact, ADR, BPMN, rule hoặc implementation được tạo.

Nguồn task history vẫn là workspace-only. Nó giúp truy vết quá trình xây dựng hệ thống nhưng không override `docs/`.

Không scan toàn bộ `llm-wiki/tasks/archive/` theo mặc định. Với task mới không có dấu hiệu liên quan đến lịch sử, chỉ ghi rằng archive history không được scan vì chưa cần thiết.

### Quy Tắc Đặt Tên Archive

Khi archive, dùng format:

```text
llm-wiki/tasks/archive/YYYYMMDD_NNN_short-name/
```

Ví dụ:

```text
llm-wiki/tasks/archive/20250912_005_adjust-threshold/
llm-wiki/tasks/archive/20260513_001_task-workspace-setup/
```

Dùng `NNN` có 3 chữ số để sort đúng thứ tự.

### Trạng Thái Khuyến Nghị

| Giai đoạn | File chính | Status gợi ý |
|---|---|---|
| Mới nhận raw input | `raw.md` | `observed` |
| Đã phân tích thành task brief | `intake.md` | `synthesized` |
| Đã có quyết định | `decision-log.md` | `verified` nếu có source/human confirm |
| Đã promote sang docs | `promotion.md` | `promoted` |
| Task cũ không còn dùng | `archive/` folder | `stale` hoặc giữ nguyên theo note |

### Quy Tắc Source Of Truth

1. `llm-wiki/tasks/` chỉ là workspace memory.
2. Raw request trong `raw.md` không phải source of truth cho implementation.
3. Official source of truth nằm trong `docs/`, `tests/features`, `tests/bdd`, contract, ADR, QA matrix, runbook, hoặc explicit user decision.
4. Nếu task tạo ra thay đổi chính thức, ghi target trong `promotion.md` và cập nhật artifact trong `docs/` khi được scope cho phép.
5. Không promote assumption chưa được xác nhận.

## Mẫu Nội Dung Tối Thiểu Cho `TASK.md`

```text
Request summary:
Task type:
Impacted domain/runtime model:
Artifact layer:
Expected output:
Files in scope:
Files out of scope:
Forbidden actions:
Verification expectation:
Ready status:
```

Với task phân tích yêu cầu, sửa đổi nghiệp vụ, hoặc package/release, thêm:

```text
Task profile:
Source-of-truth status:
Acceptance/evidence gap:
Promotion targets:
Human decision needed:
```

## Mẫu Nội Dung Tối Thiểu Cho `promotion.md`

```text
Promotion needed:
Reason:
Target artifact layer:
Target path:
Manifest update needed:
Sources update needed:
Prompts update needed:
```

## Related

- [agent_request_lifecycle_kms_sdd_grounding.md](./agent_request_lifecycle_kms_sdd_grounding.md)
- [Requirements](../01-requirements/00.requirement.md)
- [BPMN](../03-business-analysis/09.bpmn.md)
- [Definition Of Ready](../09-runbook/17.dor.md)
- [Definition Of Done](../09-runbook/18.dod.md)
- [QA Matrix](../06-quality-assurance/14.qa-matrix.md)
- [tasks README](../../llm-wiki/tasks/README.md)
- [8.archive-current-task.md](../grounding/skills/8.archive-current-task.md)
- [9.create-current-task.md](../grounding/skills/9.create-current-task.md)

## References

- [AGENTS.md](../../AGENTS.md)
- [manifest.md](../grounding/manifest.md)
- [sources.md](../grounding/sources.md)
- [prompts.md](../grounding/prompts.md)

## Change Notes

- 2026-05-13: Created official methodology for task workspace creation and archive.
- 2026-05-13: Updated task archive target to `llm-wiki/tasks/archive/` and clarified that Skill 8 cleans `current` after archive.
- 2026-05-13: Added task profiles for requirement analysis, business behavior change, and package/release tasks.
- 2026-05-13: Added promotion planning options for the three task profiles.
