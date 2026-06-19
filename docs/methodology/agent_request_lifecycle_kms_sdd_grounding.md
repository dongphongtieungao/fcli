---
id: agent-request-lifecycle-kms-sdd-grounding
title: Agent Request Lifecycle with KMS, SDD, Guardrails, Grounding and Workspace
type: methodology
category: methodology
status: published
owner: governance
domain: agentic-sdlc
project: ftransform
workspace: docs
created: '2026-04-29'
updated: 2026-05-14
sensitivity: internal
truth_level: official
source_policy: source_of_truth
source_of_truth:
- AGENTS.md
- docs/grounding/manifest.md
- docs/grounding/manifest.yaml
- docs/grounding/sources.md
- docs/grounding/prompts.md
- docs/grounding/skills
- docs/methodology/Task.md
- docs/methodology/o_2.kms_intergrate.md
- docs/methodology/o_3.kms_obsidian_rg_runbook.md
- docs/methodology/o_4.code_intelligence_agent_v2.md
- docs/methodology/o_5.agent_verification_model_v2_reviewed.md
- tools/kb.ps1
- docs/00-governance/metadata-schema.md
- docs/00-governance/taxonomy.md
related:
- docs/methodology/knowledge_base.md
- docs/methodology/methodology.md
- docs/00-governance/metadata-schema.md
- docs/00-governance/taxonomy.md
- docs/09-runbook/18.dod.md
- docs/06-quality-assurance/14.qa-matrix.md
- llm-wiki/index.md
- docs/methodology/Task.md
- llm-wiki/codebase-map.md
- docs/grounding/manifest.md
- docs/grounding/skills
upstream: []
downstream: []
tags:
  - sdd/methodology
  - sdd/reusable
  - sdd/generic
- kms/metadata
- kms/taxonomy
- kms/obsidian
- kms/rg
---

# Vòng Đời Xử Lý Request Của Agent Theo KMS, SDD, Guardrails, Grounding Và Workspace

## 1. Mục Đích

Tài liệu này mô tả vòng đời thực tế của một request trong repo `ftransform`; bản này được rà soát cập nhật lần gần nhất vào 2026-05-12.

Nó chuẩn hóa cách agent đi từ request của user tới grounding, retrieval, đối chiếu docs/tests/src/workspace, thực thi có kiểm soát, verification, cập nhật `llm-wiki/`, và promote sang `docs/` khi thật sự có thay đổi source of truth.

Trong [knowledge_base.md](./knowledge_base.md), tài liệu này nối các trụ cột từ KMS Foundation, retrieval, planning, implementation, verification đến knowledge sync thành một lifecycle request cụ thể.

Tài liệu này không thay thế:

1. `AGENTS.md`, là policy vận hành bắt buộc cho agent.
2. `docs/grounding/manifest.md`, là human-readable grounding entrypoint.
3. `docs/grounding/manifest.yaml`, là machine-readable source registry.
4. `docs/methodology/o_5.agent_verification_model_v2_reviewed.md`, là mô hình verification chi tiết.

## 2. Trạng Thái Repo Hiện Tại

Mô hình hiện tại trong repo:

| Surface | Trạng thái | Vai trò |
|---|---|---|
| `docs/` | Tồn tại | KMS chính thức và source of truth cho requirements, architecture, business analysis, specs, QA, runbooks, release, grounding, methodology |
| `docs/grounding/manifest.md` | Tồn tại, `published` | Bản đồ grounding cho human |
| `docs/grounding/manifest.yaml` | Tồn tại, version `0.2.1`, `updated: 2026-05-14` | Registry máy đọc |
| `docs/grounding/sources.md` | Tồn tại | Catalog source ID, owner, coverage, usage rules |
| `docs/grounding/prompts.md` | Tồn tại | Prompt governance và lane routing |
| `llm-wiki/` | Tồn tại | Controlled workspace của agent, không phải source of truth |
| `llm-wiki/tasks/` | Tồn tại | Task workspace và task history để truy vết quá trình xử lý request |
| `tests/features/` | Tồn tại | Feature/spec-adjacent acceptance surfaces |
| `tests/bdd/` | Tồn tại | BDD support và acceptance checks |
| `src/` | Tồn tại | Current implementation state |
| `tools/kb.ps1` | Tồn tại | Helper PowerShell cho retrieval/audit/context |
| `tools/kms_ci_audit.py` | Tồn tại | Generic KMS/SDD audit gate cho manifest, sources, critical links, contradictions, và verification evidence |
| `AGENTS.md` | Tồn tại | Canonical execution policy |

Các file methodology hiện tại có liên quan trực tiếp:

1. `docs/methodology/o_2.kms_intergrate.md`: thiết kế Lightweight KMS bằng `rg`, grounding, Obsidian metadata và `llm-wiki`.
2. `docs/methodology/o_3.kms_obsidian_rg_runbook.md`: runbook vận hành KMS, Obsidian, `rg`, PowerShell và workspace.
3. `docs/methodology/o_4.code_intelligence_agent_v2.md`: mô hình Code Intelligence và module notes trong `llm-wiki`.
4. `docs/methodology/o_5.agent_verification_model_v2_reviewed.md`: mô hình nghiệm thu bằng evidence.

## 3. Nguyên Tắc Nền

Vòng đời request không phải là:

```text
Nhận task
Đọc code
Sửa code
Chạy test
Báo xong
```

Vòng đời đúng trong repo này là:

```text
Nhận request
  -> phân loại scope
  -> đọc AGENTS.md và grounding entrypoint
  -> retrieve context tối thiểu
  -> đối chiếu docs, tests, llm-wiki, src
  -> kiểm tra SDD gates
  -> task brief và plan
  -> sửa đúng scope
  -> verify bằng evidence phù hợp risk
  -> cập nhật llm-wiki nếu có tri thức mới
  -> promote docs/registry nếu source of truth đổi
  -> final report có bằng chứng và rủi ro còn lại
```

Thứ tự tin cậy:

1. User task hiện tại.
2. `AGENTS.md`.
3. `docs/grounding/manifest.md`.
4. `docs/grounding/manifest.yaml`.
5. `docs/grounding/sources.md`.
6. `docs/grounding/prompts.md`.
7. Official artifacts trong `docs/`.
8. `tests/features/` hoặc `tests/bdd/`.
9. `llm-wiki/` workspace context.
10. `src/` current implementation.
11. Agent inference.

Quy tắc diễn giải:

1. `docs/` nói hệ thống phải như thế nào.
2. `tests/` nói behavior nào đã được biểu diễn hoặc kiểm thử.
3. `llm-wiki/` nói agent đã quan sát, nghi ngờ hoặc học được gì.
4. `src/` nói implementation hiện tại đang làm gì.
5. `src/` và `llm-wiki/` không tự động thắng official docs, contract hoặc guardrails.

## 4. Vai Trò Của Từng Vùng

### 4.1. `docs/`

`docs/` là KMS chính thức của project. Durable source-of-truth updates thuộc về `docs/`.

Các vùng canonical hiện có:

| Vùng | Vai trò |
|---|---|
| `docs/00-governance/` | Governance, taxonomy, metadata schema |
| `docs/01-requirements/` | Requirement, final business requirements, use cases |
| `docs/02-architecture/` | Vision, glossary, C4, NFR, ADR, standards |
| `docs/03-business-analysis/` | Business analysis, BPMN, workflow-level business docs |
| `docs/04-spec-test/` | OpenAPI, application spec, test/spec artifacts |
| `docs/06-quality-assurance/` | Prompt guardrails, QA matrix |
| `docs/07-ci-cd-review/` | Quality matrix, review checklists |
| `docs/09-runbook/` | Runbooks, DoR, DoD |
| `docs/10-release/` | Release and handover docs |
| `docs/grounding/` | Manifest, source catalog, prompts, grounding pack |
| `docs/methodology/` | Methodology and operating model docs |

### 4.2. `llm-wiki/`

`llm-wiki/` là controlled workspace của agent, không phải official KMS.

Nó dùng cho:

1. Codebase map.
2. Module notes.
3. Source map.
4. Open questions.
5. Assumptions.
6. Contradiction log.
7. Lessons learned.
8. Verification notes.
9. Task workspace và task history trong `llm-wiki/tasks/`.

Luật chính:

```text
llm-wiki/ giúp agent không phải học lại từ đầu.
llm-wiki/ không được tự chốt nghiệp vụ hoặc override docs/.
```

### 4.2.1. `llm-wiki/tasks/`

`llm-wiki/tasks/` lưu vòng đời từng task ở mức workspace:

| Vùng | Vai trò |
|---|---|
| `llm-wiki/tasks/current/` | Workspace của task đang làm |
| `docs/methodology/Task.md` | Hướng dẫn chính thức cho skill tạo task mới, task archive và truy vết task history |
| `llm-wiki/tasks/YYYYMMDD_NNN_short-name/` | Lịch sử task đã archive |

Quy tắc:

1. Task mới được tạo bằng `docs/grounding/skills/9.create-current-task.md` từ mô tả task của user.
2. Task cũ được archive bằng `docs/grounding/skills/8.archive-current-task.md` khi `current` có nội dung hữu ích.
3. Task history có thể được tham khảo trong retrieval khi task mới liên quan đến feature, module, decision, bug, verification hoặc documentation work trước đó.
4. Task history giúp truy vết quá trình xây dựng hệ thống nhưng không override `docs/`, tests, contracts, ADR, QA matrix hoặc runbook.

### 4.3. `tests/`

`tests/` là executable/spec-adjacent evidence khi có. Trong starter state, thư mục này có thể rỗng hoặc chỉ chứa ví dụ tối thiểu; không được suy diễn behavior đã được kiểm thử nếu chưa có file test cụ thể.

Khi project mới thêm behavior, các surface nên được đặt theo loại evidence:

1. `tests/features/` cho Gherkin hoặc acceptance scenarios.
2. `tests/bdd/` cho BDD support, step catalog, hoặc acceptance checks.
3. `tests/unit/` hoặc framework tương đương cho unit tests.
4. `tests/integration/` hoặc framework tương đương cho integration tests.
5. `verify/` hoặc tool-specific checks khi cần smoke/contract verification ngoài test framework.

### 4.4. `src/`

`src/` phản ánh implementation hiện tại. Nó là evidence để hiểu hệ thống đang chạy ra sao, nhưng không tự động là policy hoặc source of truth nếu lệch với docs, contract hoặc acceptance criteria.

### 4.5. `tools/kb.ps1`

`tools/kb.ps1` hiện là helper thực tế cho retrieval và audit nhẹ.

Các command đang được implement:

```powershell
.\tools\kb.ps1 status
.\tools\kb.ps1 grounding
.\tools\kb.ps1 docs "<keyword>"
.\tools\kb.ps1 workspace "<keyword>"
.\tools\kb.ps1 specs "<keyword>"
.\tools\kb.ps1 code "<keyword>"
.\tools\kb.ps1 search "<keyword>"
.\tools\kb.ps1 blockers
.\tools\kb.ps1 audit
.\tools\kb.ps1 audit-ci
.\tools\kb.ps1 audit-metadata
.\tools\kb.ps1 missing-meta
.\tools\kb.ps1 missing-links
.\tools\kb.ps1 contradictions
.\tools\kb.ps1 ready
.\tools\kb.ps1 context "<keyword>"
.\tools\kb.ps1 trace "<feature or module>"
.\tools\kb.ps1 sync-check "<feature or module>"
```

Ý nghĩa các command chính:

| Mục đích | Dùng hiện tại |
|---|---|
| Xem policy/KMS snapshot | `.\tools\kb.ps1 status` |
| Xem grounding pack | `.\tools\kb.ps1 grounding` |
| Search tổng hợp docs/workspace/tests/src | `.\tools\kb.ps1 search "<keyword>"` |
| Search docs chính thức | `.\tools\kb.ps1 docs "<keyword>"` |
| Search specs/tests | `.\tools\kb.ps1 specs "<keyword>"` |
| Search source implementation | `.\tools\kb.ps1 code "<keyword>"` |
| Search workspace | `.\tools\kb.ps1 workspace "<keyword>"` |
| Tìm blocker markers | `.\tools\kb.ps1 blockers` |
| Audit tổng hợp grounding, metadata, links, blockers, contradictions | `.\tools\kb.ps1 audit` |
| CI/audit gate cho KMS/SDD | `.\tools\kb.ps1 audit-ci` hoặc `python tools/kms_ci_audit.py` |
| Audit metadata | `.\tools\kb.ps1 audit-metadata` |
| Audit missing metadata | `.\tools\kb.ps1 missing-meta` |
| Audit missing links | `.\tools\kb.ps1 missing-links` |
| Tìm contradiction | `.\tools\kb.ps1 contradictions` |
| Check readiness tổng quan | `.\tools\kb.ps1 ready` |
| Tạo context pack theo keyword | `.\tools\kb.ps1 context "<keyword>"` |
| Trace một feature/module qua docs, tests, workspace, src | `.\tools\kb.ps1 trace "<feature or module>"` |
| Kiểm tra đồng bộ docs/spec/tests/workspace/src | `.\tools\kb.ps1 sync-check "<feature or module>"` |

## 5. Vòng Đời Request Chuẩn

### Giai Đoạn 0. Khởi Tạo Task Workspace

Khi nhận một request mới có khả năng cần phân tích, sửa file, verify, hoặc lưu lại decision:

1. Kiểm tra `llm-wiki/tasks/current/`.
2. Nếu `current` có nội dung hữu ích từ task trước, dùng Skill 8 để archive sang `llm-wiki/tasks/YYYYMMDD_NNN_short-name/`.
3. Dùng Skill 9 với mô tả request mới để tạo `llm-wiki/tasks/current/`.
4. Skill 9 phải phân tích mục tiêu task, task type, artifact layer, impacted domain/runtime model, verification expectation và open questions ban đầu.
5. Nếu request có dấu hiệu nối tiếp việc cũ, Skill 9 search task history trong `llm-wiki/tasks/YYYYMMDD_NNN_short-name/` và ghi task history liên quan vào `current/TASK.md` hoặc `current/intake.md`.

Output tối thiểu của giai đoạn này:

```text
Current task workspace:
Task objective:
Task type:
Artifact layer:
Related archived tasks:
Next skill:
```

### Giai Đoạn 1. Phân Loại Request

Agent xác định:

1. Request type: coding, bug fix, refactor, documentation, analysis, verification, KMS, guardrails, SDD artifact.
2. Affected domain/module.
3. Expected output.
4. Có cần sửa code không.
5. Có cần sửa docs không.
6. Có cần update `llm-wiki/` không.
7. Verification cần ở mức nào.

Output tối thiểu:

```text
Request Type:
Affected Domain/Module:
Expected Output:
Risk Level:
Likely Artifacts:
```

### Giai Đoạn 2. Grounding Entrypoint

Với task không tầm thường, đọc theo thứ tự:

1. `AGENTS.md`.
2. `docs/grounding/manifest.md`.
3. `docs/grounding/manifest.yaml` nếu cần registry máy đọc.
4. `docs/grounding/sources.md` nếu cần source ID, owner, coverage.
5. `docs/grounding/prompts.md` nếu cần prompt lane routing.
6. Official docs liên quan.
7. Tests/spec-adjacent evidence liên quan.
8. Workspace notes liên quan trong `llm-wiki/`.
9. `src/` để xác nhận hiện trạng implementation.

Nếu file grounding không tồn tại, không bịa nội dung. Ghi missing artifact vào task brief hoặc `llm-wiki/open-questions.md` nếu nó là working gap đáng lưu.

### Giai Đoạn 3. Retrieval Tối Thiểu

Ưu tiên dùng `tools/kb.ps1` khi command phù hợp đã tồn tại:

```powershell
.\tools\kb.ps1 grounding
.\tools\kb.ps1 search "<keyword>"
.\tools\kb.ps1 docs "<keyword>"
.\tools\kb.ps1 specs "<keyword>"
.\tools\kb.ps1 workspace "<keyword>"
.\tools\kb.ps1 code "<keyword>"
.\tools\kb.ps1 blockers
.\tools\kb.ps1 context "<keyword>"
.\tools\kb.ps1 trace "<feature or module>"
.\tools\kb.ps1 sync-check "<feature or module>"
```

Khi task có dấu hiệu nối tiếp việc cũ, tìm thêm trong task history:

```powershell
rg -n --heading --smart-case "<keyword>" llm-wiki/tasks
```

Nếu cần `rg` trực tiếp:

```powershell
rg -n --heading --smart-case "<keyword>" docs
rg -n --heading --smart-case "<keyword>" tests
rg -n --heading --smart-case "<keyword>" llm-wiki
rg -n --heading --smart-case "<keyword>" src
rg -n --heading --smart-case "BLOCKED|TBD|TODO|FIXME|unclear|assumption|open question|contradiction" docs llm-wiki tests
```

Chiến lược:

1. Search official docs trước.
2. Search tests/specs kế tiếp.
3. Search `llm-wiki/` để biết observation, assumption, contradiction cũ.
4. Search `llm-wiki/tasks/` khi cần truy vết task history hoặc quá trình xây dựng hệ thống.
5. Search `src/` để xác nhận implementation hiện tại.
6. Nếu ít kết quả, tìm synonym, tiếng Anh và tiếng Việt.
7. Nếu quá nhiều kết quả, lọc theo path, module, domain, status, category, hoặc tag.
8. Dùng `trace` khi cần thấy cùng một feature/module xuất hiện ở docs, tests, workspace và src.
9. Dùng `sync-check` khi cần đánh giá liên kết docs/spec/tests/workspace/src là `SYNCED`, `PARTIALLY_SYNCED` hay `NOT_SYNCED`.

### Giai Đoạn 4. Đối Chiếu Hiện Trạng

Agent phải đối chiếu bốn lớp:

1. `docs/`: expected behavior.
2. `tests/`: represented/tested behavior.
3. `llm-wiki/`: observed workspace knowledge.
4. `src/`: current implementation.

Nếu mâu thuẫn:

1. Không tự chọn nguồn thuận tiện.
2. Áp dụng precedence ở mục 3.
3. Ghi hoặc cập nhật `llm-wiki/contradiction-log.md` khi mâu thuẫn đáng lưu.
4. Chỉ sửa code/docs khi scope task cho phép và source of truth đủ rõ.

### Giai Đoạn 5. SDD Gates

#### Gate 1. Grounding Gate

Phải xác định được:

1. Grounding sources.
2. Official docs liên quan.
3. Tests hoặc executable/spec-adjacent evidence liên quan.
4. Workspace notes liên quan.
5. Current implementation files liên quan.
6. Assumptions.
7. Contradictions.
8. Missing artifacts.

#### Gate 2. Ready For Implementation Gate

Không bắt đầu implementation nếu thiếu artifact bắt buộc cho task, trừ khi user cho phép partial-context work.

Artifact cần kiểm tra theo scope:

1. Vision hoặc scope document.
2. Glossary hoặc domain terms.
3. Coding standards.
4. BPMN, business flow, hoặc business rule khi task đổi behavior nghiệp vụ.
5. BDD, acceptance criteria, hoặc test scenario khi task ảnh hưởng acceptance.
6. Contract hoặc interface specification khi task đổi API/application/UI/data contract.
7. Guardrails hoặc QA rules.
8. DoR hoặc DoD khi task ảnh hưởng delivery readiness.

Nếu thiếu artifact blocking, output `BLOCKED`, liệt kê artifact thiếu và đề xuất bước bổ sung.

#### Gate 3. Implementation Gate

Chỉ implement khi:

1. Scope rõ.
2. Grounding sources rõ.
3. Không có blocking contradiction.
4. Không có blocking open question.
5. Plan nêu rõ files in scope và out of scope.
6. Verification method rõ.

#### Gate 4. Verification Gate

Không kết luận done nếu chưa có evidence phù hợp.

Evidence có thể là:

1. Unit test.
2. Integration test.
3. BDD scenario.
4. Contract check.
5. Static analysis.
6. Review checklist.
7. Manual verification có mô tả rõ.
8. Sync/reconciliation check giữa docs, tests, code.
9. KMS/SDD audit gate: `.\tools\kb.ps1 audit-ci` hoặc `python tools/kms_ci_audit.py`; dùng `.\tools\kb.ps1 audit` để lấy thêm audit chi tiết khi cần.

Mức verification phải tương xứng task type theo `docs/methodology/o_5.agent_verification_model_v2_reviewed.md`.
KMS/SDD hoặc documentation-governance changes phải chạy audit phù hợp. Nếu `audit-ci` fail, ghi rõ blocker, xử lý các gap trong scope, và dùng targeted scans để giải thích phần còn lại.

## 6. Task Brief Và Plan

Trước khi sửa file cho task không tầm thường, agent cần task brief:

```text
Task Scope:
Grounding Sources:
Official Docs Used:
Executable Specs or Tests Used:
Workspace Notes Used:
Task History Used:
Current Implementation Findings:
Assumptions:
Contradictions:
Open Questions:
Plan:
Verification Method:
Files In Scope:
Files Out Of Scope:
```

Plan hợp lệ phải nêu:

1. Sửa file nào.
2. Vì sao sửa file đó.
3. Dựa trên source nào.
4. Không sửa file nào.
5. Rủi ro chính.
6. Verification sẽ chạy hoặc sẽ thực hiện.
7. Có cần update `llm-wiki/` không.
8. Có cần archive/update `llm-wiki/tasks/current/` không.
9. Có cần update `docs/grounding/manifest.md`, `manifest.yaml`, hoặc `sources.md` không.

## 7. Thực Thi Có Kiểm Soát

Khi thực thi:

1. Sửa tối thiểu theo plan.
2. Không refactor opportunistic.
3. Không thêm business rule ngoài grounding.
4. Không thêm field, status, validation, endpoint, hoặc error behavior ngoài contract/spec.
5. Không sửa public contract nếu không update official spec.
6. Không sửa module ngoài scope.
7. Không bỏ qua guardrails, ADR, standards, hoặc QA matrix liên quan.
8. Không gọi file generation là full validation nếu chưa kiểm rule nghiệp vụ, contract hoặc QA surface.

## 8. Verification Và Final Verdict

Sau khi sửa:

1. Nêu command hoặc check đã thực hiện.
2. Nêu source xác nhận command nếu có.
3. Nêu exit code nếu có command.
4. Nêu output path nếu có artifact.
5. Nêu pass/fail.
6. Đọc warning/error và phân loại.
7. Nếu không chạy được test, nêu lý do và residual risk.

Verdict nên dùng:

| Verdict | Khi dùng |
|---|---|
| `VERIFIED` | Evidence phù hợp scope đã pass, warning đã được đọc và không còn blocking risk |
| `PARTIALLY_VERIFIED` | Có evidence nhưng thiếu một phần do môi trường, data, hoặc artifact |
| `NOT_VERIFIED` | Chưa có evidence đủ tin cậy |
| `BLOCKED` | Thiếu source of truth, contradiction, open question, hoặc command/environment blocking |

## 9. Update `llm-wiki/`

Update `llm-wiki/` khi task tạo hoặc phát hiện tri thức mới:

1. Structure hoặc flow implementation mới hiểu được.
2. Module behavior đáng lưu.
3. Missing docs hoặc missing tests.
4. Mismatch giữa docs, tests và code.
5. Assumption hoặc open question.
6. Contradiction.
7. Risk.
8. Lessons learned.
9. Verification notes.
10. Task memory trong `llm-wiki/tasks/current/`.

File đích gợi ý:

1. `llm-wiki/codebase-map.md`
2. `llm-wiki/module-notes/<module>.md`
3. `llm-wiki/open-questions.md`
4. `llm-wiki/assumptions.md`
5. `llm-wiki/contradiction-log.md`
6. `llm-wiki/lessons-learned.md`
7. `llm-wiki/source-map.md`
8. `llm-wiki/tasks/current/`

Không update `llm-wiki/` chỉ để ghi lại thông tin hiển nhiên hoặc noise.

## 10. Promote Sang `docs/` Và Registry

Chỉ update `docs/` khi official source of truth thay đổi.

Các trường hợp hợp lệ:

1. Requirement được xác nhận hoặc đổi.
2. Business flow đổi.
3. Spec hoặc contract đổi.
4. ADR đổi.
5. Guardrail đổi.
6. QA matrix hoặc review checklist đổi.
7. DoR hoặc DoD đổi.
8. Runbook đổi.
9. Official artifact mới được tạo.

Khi official artifact mới được thêm, hoặc path/status/owner/coverage đổi, cập nhật registry liên quan:

1. `docs/grounding/manifest.md`
2. `docs/grounding/manifest.yaml`
3. `docs/grounding/sources.md` nếu cần source ID/catalog entry

Nếu chỉ là observation của agent, giữ trong `llm-wiki/`.

## 10.1. Archive Task History

Cuối task, nếu `llm-wiki/tasks/current/` có nội dung hữu ích, archive bằng Skill 8:

```text
llm-wiki/tasks/current/
  -> llm-wiki/tasks/YYYYMMDD_NNN_short-name/
```

Archive task không phải promotion sang `docs/`. Nó là cách giữ task memory để agent sau này truy vết:

1. Raw input hoặc task description ban đầu.
2. Intake và phân loại task.
3. Decision đã chốt.
4. Open questions và assumptions.
5. Verification notes.
6. Promotion target hoặc lý do không promote.
7. Các task history liên quan đã tham khảo.

Sau archive, task tiếp theo phải dùng Skill 9 để tạo `current` từ mô tả task mới.

## 11. Final Report

Format chuẩn sau task:

```text
Files Changed:
Verification Performed:
Verification Result:
Workspace Updates:
Task Archive:
Docs Updates:
Manifest Updates:
Remaining Risks:
Next Actions:
```

Nếu là code change, bổ sung:

1. Impacted module.
2. Grounding sources đã đọc.
3. Specs consulted.
4. Validation command.
5. Exit code.
6. Warning hoặc error.
7. KMS/SDD audit result khi task thay đổi policy, docs registry, grounding, KMS, hoặc verification evidence.

Nếu là documentation change, bổ sung:

1. Impacted methodology/artifact layer.
2. Grounding sources consulted.
3. Related document links.
4. Manifest/catalog updates nếu có.
5. Traceability gaps nếu còn.

## 12. Khi Nào Request Bị Chặn

Agent phải báo `BLOCKED` hoặc dừng khi:

1. Thiếu source of truth cần thiết.
2. Artifact bắt buộc không tồn tại.
3. Có blocking contradiction.
4. Có blocking open question.
5. User yêu cầu làm trái guardrails.
6. User yêu cầu sửa vượt scope mà chưa có authorization.
7. Không đủ bằng chứng nhưng task yêu cầu chốt business logic.
8. Validation command không rõ canonical source.
9. Markdown change làm mất traceability hoặc tạo fact không grounding.

## 13. Ví Dụ Mapping Thực Tế

Request mẫu:

```text
Sửa lỗi batch job không sinh đúng output summary khi chạy smoke mode.
```

Lifecycle đúng:

1. Phân loại: bug fix, runtime output generation, selected workflow mode, smoke mode.
2. Grounding: đọc `AGENTS.md`, `docs/grounding/manifest.md`, `manifest.yaml`, prompt lane runtime hoặc validation nếu liên quan.
3. Retrieval:
   ```powershell
   .\tools\kb.ps1 search "output summary smoke"
   .\tools\kb.ps1 docs "output summary"
   .\tools\kb.ps1 specs "mock"
   .\tools\kb.ps1 workspace "output summary"
   .\tools\kb.ps1 code "<feature keyword>"
   .\tools\kb.ps1 trace "output summary"
   ```
4. Reconcile: đối chiếu docs, tests/features, `llm-wiki/`, và source module liên quan.
5. Gate: xác định có đủ contract/acceptance/runbook/verification command không.
6. Task brief: nêu sources, assumptions, contradictions, plan, files in/out of scope.
7. Execute: sửa tối thiểu trong module liên quan.
8. Verify: chạy targeted test hoặc command canonical từ runbook/QA/DoD; đọc output; dùng `.\tools\kb.ps1 sync-check "output summary"` nếu cần kiểm docs/spec/tests/workspace/src đang khớp tới đâu.
9. Workspace update: ghi module finding hoặc contradiction nếu phát hiện tri thức mới.
10. Docs promotion: chỉ update docs nếu behavior chính thức hoặc runbook/spec sai.
11. Final report: files changed, verification result, risk còn lại.

## 14. Kết Luận

Vòng đời request hiện tại của repo là:

```text
Request
  -> Scope
  -> Grounding
  -> Retrieval
  -> Reconciliation
  -> SDD gates
  -> Task brief
  -> Plan
  -> Minimal execution
  -> Verification
  -> llm-wiki update when useful
  -> task archive when useful
  -> docs/registry promotion only when official truth changes
  -> final report with evidence
```

Cách hiểu ngắn nhất:

```text
docs/ là KMS chính thức.
AGENTS.md là policy vận hành agent.
docs/grounding/ là grounding entrypoint và registry.
tests/ là executable/spec-adjacent evidence khi có.
src/ là hiện trạng implementation.
llm-wiki/ là workspace quan sát, không phải source of truth.
llm-wiki/tasks/ là task memory để truy vết request lifecycle và lịch sử triển khai.
tools/kb.ps1 và rg là retrieval layer hiện tại.
verification phải có evidence tương xứng risk.
```

## Change Notes

- 2026-05-11: Updated manifest version/date after adding `docs/methodology/knowledge_base.md` as the official onboarding and operations training map.
- 2026-05-12: Updated manifest version/date after adding `docs/grounding/skills/` as the operational SDD skill pack.
- 2026-05-13: Added task workspace creation and archive to the agent request lifecycle.
