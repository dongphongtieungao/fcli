---
id: methodology-knowledge-base
title: Chuỗi 21 kiến thức nền cho SDD Agentic Development
type: methodology
category: methodology
status: published
owner: governance
domain: agentic-sdlc
project: fcli
created: 2026-05-11
updated: 2026-05-14
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/manifest.yaml
  - docs/grounding/sources.md
  - docs/grounding/prompts.md
  - docs/methodology/methodology.md
  - docs/methodology/d_3.document_strategy_and_creation_order.md
  - docs/methodology/d_1.quickstarts.md
  - docs/methodology/d_5.agentic_env_setup.md
  - docs/methodology/o_2.kms_intergrate.md
  - docs/methodology/o_3.kms_obsidian_rg_runbook.md
  - docs/methodology/o_4.code_intelligence_agent_v2.md
  - docs/methodology/o_5.agent_verification_model_v2_reviewed.md
  - docs/methodology/agent_request_lifecycle_kms_sdd_grounding.md
  - docs/06-quality-assurance/13.prompt-guardrails.md
  - docs/06-quality-assurance/14.qa-matrix.md
  - docs/07-ci-cd-review/16.review-checklists.md
related:
  - docs/methodology/methodology.md
  - docs/methodology/d_2.idea_to_spec_before_design.md
  - docs/methodology/d_3.document_strategy_and_creation_order.md
  - docs/methodology/d_1.quickstarts.md
  - docs/methodology/d_5.agentic_env_setup.md
  - docs/methodology/o_2.kms_intergrate.md
  - docs/methodology/o_3.kms_obsidian_rg_runbook.md
  - docs/methodology/o_4.code_intelligence_agent_v2.md
  - docs/methodology/o_5.agent_verification_model_v2_reviewed.md
  - docs/methodology/agent_request_lifecycle_kms_sdd_grounding.md
  - docs/00-governance/metadata-schema.md
  - docs/00-governance/taxonomy.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
  - docs/grounding/prompts.md
  - docs/02-architecture/03.vision.md
  - docs/02-architecture/02.glossary.md
  - docs/02-architecture/04.architecture-c4.md
  - docs/03-business-analysis/09.bpmn.md
  - docs/04-spec-test/application-spec.md
  - docs/06-quality-assurance/13.prompt-guardrails.md
  - docs/06-quality-assurance/14.qa-matrix.md
  - docs/07-ci-cd-review/16.review-checklists.md
  - docs/09-runbook/17.dor.md
  - docs/09-runbook/18.dod.md
tags:
  - sdd/methodology
  - sdd/reusable
  - sdd/generic
---
# Chuỗi 21 kiến thức nền cho SDD Agentic Development

## Purpose

Tài liệu này là bản đồ đào tạo và vận hành cho member mới khi tiếp cận mô hình **Spec-Driven Development (SDD) + KMS + Agentic Workflow** trong repository hiện tại.

Mục tiêu ban hành:

- Giảm gap tri thức giữa vận hành truyền thống và agentic software delivery.
- Chuẩn hóa thành một chuỗi 21 trụ cột duy nhất, đủ chi tiết để dùng cho onboarding, task execution, verification, handover và training vận hành.
- Nối từng năng lực với artifact thật trong repo để người học biết phải đọc gì, kiểm gì, và khi nào phải dừng/escalate.
- Giúp human và agent cùng làm việc theo source, gate, evidence và traceability thay vì dựa vào chat tự do.

Tài liệu này là **methodology onboarding guide**. Nó không thay thế `AGENTS.md`, grounding manifest, application spec, QA matrix, runbook, DoR/DoD hoặc model-local policy.

## Context

Repository này dùng:

- `docs/` làm KMS chính thức.
- `docs/grounding/manifest.yaml` và `docs/grounding/manifest.md` làm registry/source map.
- `tests/features/` và `tests/bdd/` làm executable/spec-adjacent evidence.
- `llm-wiki/` làm workspace ghi observation, assumption, contradiction và lesson learned, không phải source of truth.

Chuỗi Spec First cốt lõi của repo:

`Vision/Glossary -> C4/NFR/ADR/Standards -> BPMN -> Executable Specification bằng Feature/BDD -> Contract/Spec -> Guardrails/QA -> DoR/DoD -> Code/Test/Verification -> Handover`

Tài liệu này diễn giải chuỗi đó thành chương trình học cho member vận hành.

## Đối Tượng Sử Dụng

Tài liệu này dành cho các nhóm cần tham gia vận hành hoặc phối hợp delivery theo mô hình SDD + KMS + Agentic Workflow, đặc biệt khi họ chưa quen làm việc với coding agent.

| Đối tượng | Dùng tài liệu này để làm gì | Kết quả mong đợi |
|---|---|---|
| Member vận hành mới | Hiểu quy trình, thuật ngữ, gate và trách nhiệm khi dùng agent | Biết đọc nguồn, phân loại task/risk, kiểm evidence và escalate đúng lúc. |
| BA/PM hoặc domain owner | Hiểu artifact nào cần có trước khi giao task cho agent | Biết chuẩn hóa requirement, AC, open question và source-of-truth. |
| Developer hoặc implementation owner | Chuẩn bị task brief, scope, diff review và verification cho agent | Biết giữ code trong boundary, kiểm diff và không để agent vượt spec. |
| QA hoặc reviewer | Chọn evidence phù hợp, đọc log/warning/error và xác định verdict | Biết phân biệt pass thật, partial pass, failed, blocked và needs human review. |
| Tech lead hoặc release owner | Kiểm readiness, approval, rollback, handover và knowledge sync | Biết quyết định accept/reject/release dựa trên evidence thay vì lời hứa của agent. |

Tài liệu này không dành để thay thế training chuyên sâu về ngôn ngữ lập trình, framework, CI/CD, bảo mật, hoặc domain cụ thể của dự án mới. Những chủ đề đó cần học thêm từ artifact chuyên ngành và người phụ trách tương ứng.

## Kiến Thức Cơ Sở Cần Có

Người học không cần là senior developer, nhưng nên có nền tảng tối thiểu dưới đây để tiếp thu tài liệu hiệu quả.

| Nhóm kiến thức nền | Mức tối thiểu cần có | Nếu chưa có thì bắt đầu từ đâu |
|---|---|---|
| Vận hành hệ thống | Biết đọc runbook, log, lỗi thường gặp, checklist bàn giao | `docs/09-runbook/`, `docs/09-runbook/17.dor.md`, `docs/09-runbook/18.dod.md` |
| Git cơ bản | Biết `git status`, changed files, diff, commit/rollback ở mức khái niệm | Học trước workflow diff/review trước khi cho agent sửa code. |
| application và shell cơ bản | Biết chạy command, đọc exit code, phân biệt stdout/stderr, hiểu path | Dùng PowerShell command trong runbook và `tools/kb.ps1`. |
| Markdown và tài liệu kỹ thuật | Biết đọc heading, table, link, frontmatter, change notes | Đọc `docs/AGENTS.md` và các tài liệu methodology trong `docs/methodology/`. |
| Testing căn bản | Biết unit/integration/smoke/manual verification khác nhau | Đọc `docs/06-quality-assurance/14.qa-matrix.md` và `docs/methodology/o_5.agent_verification_model_v2_reviewed.md`. |
| AI/coding agent căn bản | Hiểu agent có thể đọc file, sửa file, chạy command nhưng vẫn có thể sai | Đọc `AGENTS.md`, `docs/grounding/prompts.md`, `docs/06-quality-assurance/13.prompt-guardrails.md`. |
| Bảo mật dữ liệu | Biết không đưa secret/PII/connection string vào prompt, log hoặc commit | Đọc guardrails và review checklist trước khi xử lý task có dữ liệu nhạy cảm. |

Kiến thức có thể học song song trong quá trình training:

- SDD và thứ tự artifact.
- KMS, grounding manifest và source precedence.
- Feature/BDD và acceptance criteria.
- Build-vs-buy và dependency governance.
- Debugging, RCA và evidence-based verification.

## Đối Tượng Và Mức Học

### Level 0. Người mới tiếp cận

Mục tiêu: hiểu từ vựng và nguyên tắc an toàn.

Member cần trả lời được:

- SDD, KMS, Agentic Workflow là gì?
- `docs/`, `tests/`, `llm-wiki/`, `src/` khác nhau thế nào?
- Vì sao không được để agent tự bịa business rule?
- Vì sao không claim pass nếu chưa có evidence?

### Level 1. Người vận hành task chuẩn

Mục tiêu: nhận request, phân loại risk, tìm nguồn, dùng prompt/playbook có sẵn.

Member cần làm được:

- Đọc `AGENTS.md`, grounding manifest và artifact liên quan.
- Phân loại task: docs-only, analysis, bug fix, code change, contract change, release task.
- Chọn verification nhẹ nhất nhưng có ý nghĩa.
- Biết khi nào chỉ được analysis, không được cho agent sửa code.

### Level 2. Người điều phối agent

Mục tiêu: dùng Codex an toàn trong repo.

Member cần làm được:

- Tạo task brief có scope, sources, files in/out, risk và verification method.
- Đọc `git status`, changed files và diff.
- Yêu cầu agent sửa trong boundary, kiểm diff, chạy/đọc evidence.
- Ghi lại open questions, assumptions hoặc contradiction đúng chỗ.

### Level 3. Người nghiệm thu và bàn giao

Mục tiêu: quyết định accept/reject/rollback/escalate.

Member cần làm được:

- Đọc log, warning, error, stack trace và output artifact.
- Phân biệt `VERIFIED`, `PARTIALLY VERIFIED`, `FAILED`, `BLOCKED`, `NEEDS HUMAN REVIEW`.
- Kiểm release readiness, rollback note, known issue và handover.
- Không để AI quyết định thay human ở business, security, architecture hoặc release.

## Ba Khái Niệm Nền Cần Hiểu Trước

| Khái niệm | Hiểu đơn giản | Trong repo này |
|---|---|---|
| SDD | Viết spec trước để code/test/review bám theo nguồn thật | Artifact spine từ vision/glossary đến BPMN, feature, contract, QA, DoR/DoD |
| KMS | Kho tri thức có nguồn, status, link và precedence | `docs/`, `docs/grounding/*`, `tools/kb.ps1`, metadata/frontmatter |
| Agentic Workflow | Dùng agent có tool để đọc repo, sửa file, chạy command và báo evidence | Codex làm việc theo `AGENTS.md`, grounding, diff, verification và final report |

## Chuỗi 21 Kiến Thức Theo Thứ Tự Phụ Thuộc

| # | Nhóm kiến thức | Member phải biết làm gì | Artifact chính trong repo | Gate cần đạt |
|---|---|---|---|---|
| 1 | Domain Knowledge | Hiểu thuật ngữ, phạm vi, business rule và pain point | `docs/02-architecture/02.glossary.md`, `docs/01-requirements/*` | Thuật ngữ và phạm vi domain đủ rõ; thiếu thì không cho agent tự suy đoán. |
| 2 | Software Development Life Cycle | Hiểu vòng đời từ yêu cầu đến bàn giao và vì sao không code ngay | `docs/methodology/methodology.md`, `docs/09-runbook/17.dor.md`, `docs/09-runbook/18.dod.md` | Biết mỗi phase cần artifact/evidence nào trước khi chuyển bước. |
| 3 | Problem Definition & Requirement Analysis | Chốt problem, scope, stakeholder expectation, assumption và open question | `docs/02-architecture/03.vision.md`, `docs/01-requirements/*` | Problem và phạm vi đủ rõ để không biến implementation thành suy đoán. |
| 4 | Acceptance Criteria, DoR, DoD, Task & Risk Classification | Phân loại task/risk để chọn AC, DoR/DoD và mức verification | `docs/09-runbook/17.dor.md`, `docs/09-runbook/18.dod.md`, `tests/features/*.feature` | Spec Clarity Gate: AC đo được; task/risk đủ rõ để biết được làm, dừng hay escalate. |
| 5 | Spec Clarity & Business Specification | Mô tả input/output/rule/happy path/error path, assumption và câu hỏi còn mở | `docs/01-requirements/01.use-cases.md`, `docs/03-business-analysis/09.bpmn.md` | Business spec đủ rõ cho design; thiếu thì ghi open question hoặc assumption, không bịa rule. |
| 6 | Solution Design & Software Architecture | Hiểu module boundary, component, dependency, trade-off và impact | `docs/02-architecture/04.architecture-c4.md`, `docs/02-architecture/05.nfr.md`, `docs/02-architecture/06.adr.md` | Design đủ rõ để không biến implementation thành refactor tùy hứng. |
| 7 | C4, UML, ADR, NFR & Coding Standards | Đọc artifact kiến trúc, quyết định, ràng buộc chất lượng và chuẩn code | `docs/02-architecture/04.architecture-c4.md`, `docs/02-architecture/diagrams/*`, `docs/02-architecture/06.adr.md`, `docs/02-architecture/08.standards.md` | Architecture, NFR, ADR và standards không mâu thuẫn. |
| 8 | Business Process Modeling bằng BPMN | Chuyển nghiệp vụ thành actor, step, decision point, happy/error path | `docs/03-business-analysis/09.bpmn.md` | Flow đủ rõ để sinh executable specification hoặc test case. |
| 9 | Executable Specification bằng Gherkin | Đọc/viết Given/When/Then hoặc acceptance checks | `tests/features/*.feature`, `tests/bdd/*` | Business flow được kiểm chứng bằng scenario hoặc acceptance evidence. |
| 10 | Contract & Interface Specification | Hiểu application/API/UI contract, IO, option, exit code, schema, error behavior | `docs/04-spec-test/application-spec.md`, `specs/schemas/tools/application.schema.json`, `docs/04-spec-test/10.openapi.yaml` | Ready for Implementation Gate: interface đã khóa theo loại ứng dụng và khớp flow/spec. |
| 11 | Build-vs-Buy, Application Skeleton & Technical Scaffold | Quyết định dùng thư viện, built-in hay tự code; hiểu scaffold và module boundary | `docs/02-architecture/07.project-structure.md`, `pyproject.toml`, `src/`, `tests/` | Dependency Gate: dependency mới có lý do, license/maintenance/security được review; scaffold đủ để agent implement trong scope. |
| 12 | KMS cho Agentic Development | Biết vai trò của official docs, workspace notes, source catalog và prompt routing | `docs/`, `docs/grounding/sources.md`, `docs/grounding/prompts.md`, `docs/AGENTS.md`, `llm-wiki/` | Human và agent biết tri thức nào là official, tri thức nào chỉ là observation. |
| 13 | Grounding, Retrieval & Traceability với `AGENTS.md`, manifest, `kb.ps1`, `rg` | Dùng source precedence, manifest, local policy, `rg` và `tools/kb.ps1` để dựng context có thể tái lập | `AGENTS.md`, `docs/grounding/manifest.*`, `tools/kb.ps1`, `.rgignore` | Có thể chứng minh nguồn đã đọc, truy vết từ request tới artifact/code/test và báo conflict khi có. |
| 14 | AI Literacy, Prompt Design, Rule, Guardrail & Agent Operating Policy | Phân biệt chatbot/coding assistant/agent; thiết kế prompt như execution contract; kiểm secret/PII/destructive risk | `docs/grounding/prompts.md`, `docs/grounding/prompts/*`, `docs/06-quality-assurance/13.prompt-guardrails.md` | Agent Safety Gate: prompt có role, source, scope, forbidden actions và verification method. |
| 15 | Agentic Planning với Codex | Tạo plan theo source, task type, risk, files in/out và verification | `docs/methodology/d_3.document_strategy_and_creation_order.md`, `docs/methodology/agent_request_lifecycle_kms_sdd_grounding.md` | Plan khớp spec, architecture, contract, source precedence và không vượt scope. |
| 16 | Git/Diff-Based Agentic Implementation với Codex | Đọc `git status`, changed files, diff, patch, branch/commit/PR ở mức cơ bản | `src/`, `tests/`, model-local `AGENTS.md` khi có, Git diff | Code nằm trong boundary; member đọc được diff và xác nhận thay đổi không vượt scope. |
| 17 | Automation Test Implementation | Hiểu test automation tối thiểu, fixture/mock, unit/integration/BDD và regression surface | `tests/`, `tests/features/*.feature`, `tests/bdd/*`, `verify/`, `pytest.ini` | Test mới hoặc test hiện có phản ánh behavior cần kiểm; không tạo test chỉ để pass hình thức. |
| 18 | Evidence-Based Verification | Chọn command/evidence phù hợp, đọc exit code, log, warning, error và output artifact | `docs/06-quality-assurance/14.qa-matrix.md`, `docs/methodology/o_5.agent_verification_model_v2_reviewed.md`, `docs/09-runbook/18.dod.md` | Verification Gate: evidence khớp task/risk; warning/error/output artifact được đọc và phân loại. |
| 19 | Debug, Fix Bug & Regression Check | Xác định root cause, fix tối thiểu, tránh refactor lan rộng, chạy regression phù hợp | `docs/07-ci-cd-review/16.review-checklists.md`, `docs/06-quality-assurance/14.qa-matrix.md`, `src/`, `tests/` | Debug/refinement có RCA, diff hẹp và regression evidence tương xứng. |
| 20 | Knowledge Sync vào `llm-wiki/` hoặc Promotion vào `docs/` | Ghi observation, assumption, contradiction, lesson learned hoặc promote official knowledge đúng nơi | `llm-wiki/`, `docs/`, `docs/grounding/manifest.*`, `docs/AGENTS.md` | Workspace không override docs; official knowledge được promote kèm evidence và manifest impact khi cần. |
| 21 | Release, Handover & Rollback Plan | Hiểu CI result, release note, known issues, smoke test, rollback và handover | `docs/10-release/`, `docs/09-runbook/`, `docs/09-runbook/18.dod.md` | Release Readiness Gate: có bằng chứng readiness, handover, rollback hoặc limitation rõ ràng. |

## Task Và Risk Classification

Member phải phân loại request trước khi chọn prompt hoặc cho agent sửa file.

| Task type | Ví dụ | Mức xử lý mặc định | Evidence tối thiểu |
|---|---|---|---|
| Docs-only | Sửa wording, bổ sung guide, normalize markdown | Được làm nếu source rõ | Markdown structure, link/source check, meaning preserved |
| Analysis task | Review tài liệu, phân tích gap | Analysis only, không sửa code | Sources used, findings, assumptions/open questions |
| Bug fix hẹp | Sửa lỗi rõ trong một module | Được plan và code nếu source đủ | Targeted test/log/diff review |
| Small code change | Thay đổi behavior nhỏ, scope rõ | Cần impacted model/boundary | Targeted verification và warning/error review |
| Business behavior change | Đổi rule nghiệp vụ/report logic | Escalate nếu thiếu spec/AC | Business source, acceptance mapping, test/runtime evidence |
| Contract change | application/API/UI/schema đổi | Cần review contract | Contract/spec update, compatibility/routing evidence |
| Data/security/runtime change | Data path, permission, secret, production operation | High risk, cần approval | Security/data review, rollback/mitigation, canonical command |
| Release task | Build, deploy, handover, rollback | Cần owner approve | DoD, runbook, smoke test, known issues |

Risk cues:

- Low risk: docs wording, comment, link, non-runtime metadata.
- Medium risk: local code path, test-only change, non-critical config.
- High risk: business rule, model boundary, data access, runtime command, generated output, shared architecture.
- Security/data risk: secret, PII, auth, permission, production data, destructive command.
- Release risk: artifact handed to customer/user, rollback needed, operational monitoring needed.

## Gate Model

| Gate | Điều kiện tối thiểu | Không đạt thì làm gì |
|---|---|---|
| Domain Readiness | Thuật ngữ và phạm vi cốt lõi rõ | Quay lại glossary, vision hoặc requirement. |
| Spec Clarity | AC/DoR/DoD có thể kiểm chứng; task/risk đã phân loại | Bổ sung scenario, open questions, acceptance checklist hoặc risk note. |
| Design Readiness | C4/NFR/ADR/standards không mâu thuẫn | Chốt trade-off vào ADR hoặc standards. |
| Dependency Gate | Dependency mới có lý do, license, maintenance và security risk được rà soát | Không thêm package; dùng built-in hoặc yêu cầu review. |
| Agent Safety Gate | Task type, risk, allowed files, forbidden actions, secret/data risk đã rõ | Chỉ cho phép analysis; không cho agent sửa code. |
| Human Approval Gate | Task medium/high risk đã có owner approve plan và verification method | Escalate senior/architect/QA/security tùy loại risk. |
| Ready for Implementation | Contract/spec khớp BPMN và executable spec; files in/out rõ | Sửa contract/spec hoặc scope trước khi code. |
| Verification | Có command/evidence/source xác nhận; warnings/errors được đọc | Không claim pass; chỉ ghi mức evidence thật sự đạt. |
| Release Readiness | DoD, runbook, handover, rollback/known issues và sync notes đủ | Tạo gap report hoặc hoàn thiện handover. |

## Human Approval & Escalation Model

| Khi gặp tình huống | Member được làm | Phải escalate cho |
|---|---|---|
| Docs-only, không đổi meaning | Sửa theo source và kiểm link/metadata | Không cần nếu source rõ |
| Thiếu requirement hoặc business rule | Ghi `insufficient grounding` và hỏi lại | BA/product owner/senior |
| Đổi architecture, shared module hoặc dependency | Chuẩn bị analysis và option | Architect/senior engineer |
| Đổi security, secret, auth, permission hoặc production data | Dừng ở analysis | Security/owner |
| Test fail hoặc log có warning/error chưa hiểu | Ghi evidence và RCA sơ bộ | Senior/QA |
| Release, rollback hoặc customer-facing artifact | Chuẩn bị checklist/evidence | Release owner/QA |

Nguyên tắc: AI không phải bên phê duyệt cuối. Human chịu trách nhiệm accept/reject, update source of truth và release decision.

## Thách Thức Khi Thực Hiện Roadmap

Rào cản lớn nhất không nằm ở việc "biết dùng AI", mà nằm ở việc dùng AI trong một quy trình phát triển phần mềm có kiểm soát. Member vận hành không chỉ học prompt; họ phải học một chuỗi năng lực vốn thường thuộc cấp senior: domain, requirement, spec, design, test, release, KMS, grounding, agentic workflow và verification.

Trong mô hình này, agent không được đi thẳng từ request sang code. Agent phải đi qua grounding, retrieval, đối chiếu `docs/`, `tests/`, `llm-wiki/`, `src/`, kiểm SDD gates, lập task brief, thực thi đúng scope, verify bằng evidence, rồi mới update workspace hoặc promote docs nếu source of truth thay đổi.

### Bản chất chuyển đổi

Đội vận hành thường quen với luồng:

```text
Nhận ticket
Kiểm tra lỗi
Chạy script
Sửa config
Restart service
Ghi kết quả
```

Roadmap agentic SDD yêu cầu luồng khác:

```text
Nhận request
Phân loại scope
Xác định source of truth
Truy xuất context
Đối chiếu tài liệu, test, code, workspace
Kiểm tra readiness
Lập plan
Dùng AI agent thi công
Verify bằng evidence
Cập nhật tri thức
Báo cáo rủi ro còn lại
```

Điểm khó là member phải chuyển từ tư duy làm theo thao tác sang tư duy điều phối một quy trình sản xuất phần mềm có kiểm chứng.

### Thách thức chính

| # | Thách thức | Biểu hiện thường gặp | Cách giảm rủi ro |
|---|---|---|---|
| 1 | Cognitive load quá lớn | Member phải học domain, requirement, AC, architecture, BPMN, Gherkin, contract, test, KMS, prompt, agent tool, code review, verification và release cùng lúc | Dạy theo task nhỏ, tăng dần độ khó, mỗi bước có checklist và artifact cụ thể. |
| 2 | Prompt design bị hiểu như chat thường | Prompt chỉ ghi "hãy sửa lỗi này" hoặc "hãy viết code này" | Chuẩn hóa prompt như execution contract: role, scope, sources, files in/out, forbidden actions, verification và report. |
| 3 | Khó hiểu Codex, Claude Code, Gemini coding agent | Member nghĩ đây chỉ là ChatGPT biết sửa code | Giải thích đây là agentic coding runtime có repo context, tool call, patch, command execution, sandbox, approval, diff và test output. |
| 4 | Thiếu tư duy thiết kế phần mềm | Code AI tạo chạy được trước mắt nhưng không maintain, không test, không trace được về spec | Dạy module boundary, input/output, error behavior, dependency, side effect và release impact trước khi cho sửa code thật. |
| 5 | KMS và grounding khó | Lẫn vai trò giữa `docs`, `tests`, `llm-wiki`, `src`, manifest và prompt catalog | Dùng bài tập retrieval bắt buộc: tìm source of truth, test liên quan, implementation hiện tại và observation workspace. |
| 6 | Verification khó hơn implementation | Member chạy command nhưng không biết command đó đủ nghĩa để nghiệm thu chưa | Dạy evidence theo task/risk, đọc exit code, log, warning, error, output artifact và phân loại verdict. |
| 7 | Ảo tưởng vào AI | Tin agent nói done, test pass hoặc tài liệu hợp lý mà không kiểm source/log/diff | Bắt buộc review evidence, không nghiệm thu bằng lời khẳng định của agent. |
| 8 | Khó chuyển tri thức senior thành thao tác junior | Người mới hiểu lý thuyết nhưng không biết làm task thật | Biến tri thức thành checklist, template, prompt mẫu, command mẫu, ví dụ đúng/sai, rubric và task mô phỏng. |
| 9 | Ranh giới human và agent chưa quen | Member nghĩ AI đã làm nghĩa là xong | Đào tạo rõ: agent thi công có hỗ trợ reasoning; human chốt scope, approve plan, review diff, đọc evidence và quyết định release. |
| 10 | Toolchain phức tạp | IDE, Git, Python, PowerShell, `rg`, `kb.ps1`, Codex, terminal, test runner, Markdown, KMS và CI gây quá tải | Chuẩn hóa sandbox học tập và một đường thực hành cố định trước khi mở rộng sang task thật. |
| 11 | Thiếu thời gian học sâu | Training quá học thuật thì bỏ cuộc, quá thao tác thì không hiểu bản chất | Thiết kế "học đến đâu dùng được đến đó": artifact, checklist, prompt, output mẫu và cách verify cho từng bước. |
| 12 | Khó đo năng lực sau đào tạo | Chỉ hỏi "đã hiểu chưa" nhưng không biết member làm được chưa | Đánh giá bằng task thực tế: phân loại request, tìm source, dùng `kb.ps1`, tạo task brief, đọc diff, chạy test, báo verdict. |

### Sáu nhóm rào cản

| Nhóm | Nội dung | Ví dụ cần huấn luyện |
|---|---|---|
| Nhận thức | Chưa hiểu agentic development và vai trò AI trong SDLC | Phân biệt chat AI, coding assistant và coding agent; hiểu AI không thay thế source of truth. |
| Kỹ thuật | Chưa chắc Git, application, Python, test runner, log, IDE | Đọc diff, chạy test, hiểu exit code, dùng `rg`, `kb.ps1` và terminal. |
| Phương pháp luận | Chưa hiểu domain analysis, requirement, AC, DoR, DoD, BPMN, Gherkin, contract | Tạo task brief, acceptance checklist và mapping từ flow sang spec/test. |
| KMS và grounding | Chưa hiểu source of truth, manifest, `docs`, `tests`, `llm-wiki`, `src` | Tìm đúng nguồn, không để workspace note override official docs, biết khi nào promote docs. |
| Kiểm chứng | Chưa biết evidence đủ là gì | Phân loại `VERIFIED`, `PARTIALLY VERIFIED`, `FAILED`, `BLOCKED`, `NEEDS HUMAN REVIEW`. |
| Tổ chức | Thiếu thời gian, mentor, sandbox, task mẫu, rubric và quy định approval | Chia role/level, giới hạn scope ban đầu, có reviewer và sandbox rủi ro thấp. |

### Không đào tạo tất cả thành senior

Mục tiêu thực tế không phải biến mọi member vận hành thành solution architect. Nên chia vai trò theo level:

| Level | Vai trò | Năng lực tối thiểu |
|---|---|---|
| 1 | AI Assisted Operator | Dùng prompt mẫu, chạy command mẫu, đọc kết quả cơ bản, biết báo `BLOCKED` khi thiếu source hoặc rủi ro cao. |
| 2 | Agentic Task Executor | Tạo task brief đơn giản, dùng Codex theo scope, chạy targeted test, đọc warning/error và viết final report. |
| 3 | Agentic Developer | Sửa code nhỏ, viết test, debug bug hẹp, review diff và cập nhật `llm-wiki/` đúng vai trò. |
| 4 | Agentic Reviewer | Review plan, diff, architecture impact, contract impact, QA matrix, DoD và release risk. |
| 5 | Agentic Solution Owner | Thiết kế KMS, workflow, guardrails, prompt, verification model, release policy và approval boundary. |

### Cách training nên thiết kế

Không nên đào tạo thuần tuyến tính kiểu học hết domain, SDLC, BPMN, Gherkin rồi mới tới Codex. Cách hiệu quả hơn là đi theo kịch bản task:

```text
Một request thật
Một docs set nhỏ
Một kb.ps1 command
Một prompt mẫu
Một Codex plan
Một patch nhỏ
Một test
Một verification report
Một release note
```

Sau khi làm task mẫu, trainer mới rút ra lifecycle, giải thích artifact, cho làm lại với task tương tự và tăng dần độ khó.

### Khung training thực tế

| Giai đoạn | Mục tiêu | Bài tập điển hình |
|---|---|---|
| 1. Nhận thức và an toàn | Hiểu agent không phải người quyết định cuối | Phân loại rủi ro khi tin AI quá mức, không tự merge/release. |
| 2. KMS và retrieval | Tìm đúng nguồn | Dùng `kb.ps1` hoặc `rg` tìm source of truth, test liên quan, open question hoặc contradiction. |
| 3. Request lifecycle | Đi theo flow chuẩn | Tạo task brief cho một bug giả lập, chỉ phân tích và lập plan. |
| 4. Prompt và Codex | Gọi agent đúng cách | Dùng prompt mẫu cho Codex tạo plan, so sánh plan tốt và plan kém. |
| 5. Test và verification | Không tin agent nếu thiếu evidence | Đọc verification report, phân loại verdict và chỉ ra evidence thiếu. |
| 6. Implementation task nhỏ | Làm task hẹp có kiểm soát | Sửa lỗi nhỏ, không đổi contract, không sửa ngoài scope, chạy test và báo cáo. |
| 7. Release và handover | Bàn giao được kết quả | Viết release note, known issue, rollback note, handover và remaining risk. |

Kết luận chiến lược: đây là một operating model mới cho software delivery có AI agent tham gia. Member vận hành không cần hiểu toàn bộ chiều sâu như architect ngay từ đầu; họ cần playbook, prompt chuẩn, KMS retrieval, evidence discipline, sandbox rủi ro thấp và escalation path rõ ràng.

## Roadmap Nắm Bắt Và Triển Khai SDD

Roadmap dưới đây diễn giải 21 trụ cột thành thứ tự học và triển khai thực tế. Mục tiêu không phải học thuộc thuật ngữ, mà là đạt đủ năng lực để biến request thành spec, thiết kế, implementation, test, verification, handover và knowledge sync có thể truy vết.

| Giai đoạn | Trụ cột | Mục tiêu năng lực | Artifact cần nắm | Kết quả phải tạo được |
|---|---|---|---|---|
| 1. Nền tảng domain và SDLC | 1-2 | Hiểu domain, vòng đời phát triển phần mềm và lý do phải đi theo spec trước code | Glossary, vision, methodology, DoR, DoD | Diễn giải đúng phạm vi hệ thống, thuật ngữ chính và các phase từ requirement tới handover. |
| 2. Làm rõ bài toán và tiêu chí nghiệm thu | 3-5 | Chốt problem, scope, acceptance criteria, task/risk và business specification | Requirement, use case, BPMN, DoR/DoD, feature files | Task brief có problem statement, AC, assumption, open question và risk classification. |
| 3. Thiết kế giải pháp và kiến trúc | 6-7 | Đọc và dùng architecture artifacts để giữ boundary, trade-off, NFR và coding standards | C4, UML/PlantUML, ADR, NFR, standards | Design note hoặc review note chỉ rõ module boundary, dependency và constraint cần tuân thủ. |
| 4. Mô hình hóa behavior thành spec kiểm chứng được | 8-10 | Chuyển flow nghiệp vụ thành executable specification và contract/interface spec | BPMN, Gherkin/BDD, application/OpenAPI/UI/data contract | Scenario hoặc contract đủ rõ để implementation và test không phải suy đoán. |
| 5. Chuẩn bị scaffold và môi trường thi công | 11 | Hiểu build-vs-buy, dependency, application skeleton, module layout và technical scaffold | Project structure, `pyproject.toml`, `src/`, `tests/`, dependency notes | Xác định đúng nơi sửa, dependency policy và phạm vi code/test liên quan. |
| 6. Thiết lập KMS, grounding và policy cho agent | 12-14 | Dùng KMS, manifest, retrieval, traceability, prompt policy và guardrails để agent làm việc có kiểm soát | `AGENTS.md`, manifest, sources, prompts, guardrails, `tools/kb.ps1`, `rg` | Task context có nguồn đọc, source precedence, files in/out, forbidden actions và verification method. |
| 7. Lập kế hoạch và thi công với Codex | 15-16 | Lập plan theo source, thực hiện patch hẹp, đọc diff và giữ implementation trong boundary | Task brief, Codex plan, git diff, model-local policy, code/test files | Patch đúng scope, không đổi behavior ngoài spec, không phá contract hoặc model boundary. |
| 8. Tự động hóa test và kiểm chứng bằng evidence | 17-18 | Viết/chọn test phù hợp, chạy command canonical, đọc exit code, log, warning, error và output artifact | Tests, verify scripts, QA matrix, runbook, verification model | Evidence pack phân loại đúng `VERIFIED`, `PARTIALLY VERIFIED`, `FAILED`, `BLOCKED`, hoặc `NEEDS HUMAN REVIEW`. |
| 9. Debug, regression và ổn định hóa | 19 | Xác định root cause, fix tối thiểu, chạy regression phù hợp và tránh refactor lan rộng | Logs, stack trace, review checklist, impacted tests/code | RCA ngắn, diff hẹp, regression evidence và remaining risk rõ ràng. |
| 10. Đồng bộ tri thức, bàn giao và rollback | 20-21 | Ghi observation đúng nơi, promote docs khi có evidence, chuẩn bị release/handover/rollback | `llm-wiki/`, official docs, manifest, runbook, handover, DoD | Knowledge sync note, docs promotion khi cần, handover, known issues và rollback plan. |

Luồng triển khai chuẩn:

```text
Domain -> Problem -> AC -> Business Spec -> Architecture -> BPMN -> BDD
-> Contract -> Scaffold -> KMS/Grounding -> Prompt/Plan -> Patch
-> Test -> Evidence -> Debug/Regression -> Knowledge Sync -> Handover/Rollback
```

Roadmap này cũng là thứ tự kiểm readiness. Nếu một giai đoạn trước thiếu nguồn hoặc mâu thuẫn, giai đoạn sau chỉ được làm ở mức analysis, draft, sandbox hoặc task rủi ro thấp; không được để agent tự suy diễn business rule, contract, architecture hoặc release decision.

## Checklist Cho Member Vận Hành

### Trước khi gọi agent

- `[ ]` Tôi đã đọc `AGENTS.md` và local `AGENTS.md` nếu sửa trong subtree có policy riêng.
- `[ ]` Tôi đã xác định task type và risk.
- `[ ]` Tôi đã biết source-of-truth nào cần đọc.
- `[ ]` Tôi đã xác định files in scope và files out of scope.
- `[ ]` Tôi đã biết forbidden actions: secret/PII, DML/DDL, destructive command, business rule tự bịa, cross-model logic.
- `[ ]` Tôi đã biết verification method hoặc biết cần escalate nếu chưa rõ.

### Sau khi agent trả kết quả

- `[ ]` Tôi đã xem changed files và diff.
- `[ ]` Tôi đã kiểm agent không sửa ngoài scope.
- `[ ]` Tôi đã đọc test/log/output, không chỉ nhìn exit code.
- `[ ]` Tôi đã phân loại warning/error nếu có.
- `[ ]` Tôi đã xác định verdict đúng: `VERIFIED`, `PARTIALLY VERIFIED`, `FAILED`, `BLOCKED`, hoặc `NEEDS HUMAN REVIEW`.
- `[ ]` Tôi đã cập nhật docs/manifest/workspace nếu task tạo knowledge chính thức hoặc observation bền vững.

## Chi Tiết Mục 13: Grounding, Retrieval, Dev Environment, Code Intelligence & Second Brain

Mục 13 được tách riêng vì đây là năng lực triển khai thực chiến. KMS chỉ có ý nghĩa khi member và agent tìm đúng nguồn, môi trường dev chạy được, và tri thức làm việc được ghi đúng nơi.

### Retrieval và search strategy

- `rg` là search engine mặc định cho repo vì nhanh, ổn định, dễ lọc theo path, file type và keyword.
- `tools/kb.ps1` là lớp orchestration trên `rg`, dùng cho các truy vấn lặp lại như `search`, `docs`, `specs`, `workspace`, `code`, `context`, `trace`, `sync-check`, `audit-ci`.
- Query tốt nên đi từ rộng đến hẹp: domain keyword -> artifact type -> module/path -> exact symbol hoặc source ID.
- Search không chỉ tìm nội dung; nó còn phát hiện blocker, contradiction, open question, missing metadata và drift.

Gate cần đạt: agent có thể tái lập context bằng command thay vì dựa vào trí nhớ chat.

### Dev environment setup

- Kiểm tra các tool nền như `git`, `rg`, `fd`, `jq`/`yq`, `bat`, `python`, package manager, shell phù hợp và project runner.
- Chuẩn hóa cách gọi lệnh trên PowerShell/Bash để tránh lệch môi trường.
- Phân biệt lỗi do spec/code với lỗi do environment, dependency, path, shell, encoding hoặc virtual environment.
- Không claim verification pass khi môi trường thiếu dependency hoặc command canonical chưa được resolve.

Gate cần đạt: một agent mới có thể chạy retrieval, audit và validation tối thiểu trên cùng workspace mà không phải đoán toolchain.

### Code Intelligence

Code Intelligence là năng lực hiểu codebase trước khi sửa code:

- Biết module nào là source of truth hiện tại và module nào chỉ là implementation state.
- Biết hotspot, caller/callee, file role, entry point và boundary.
- Biết khi nào cần đọc `interfaces.py`, factory, registry, SQL/spec, runbook hoặc QA matrix.
- Ghi module note và codebase map vào `llm-wiki/` khi phát hiện tri thức làm việc có ích.

Gate cần đạt: trước khi sửa code, member biết vì sao file đó đúng scope và downstream nào bị ảnh hưởng.

### Second Brain và `llm-wiki/`

- `docs/` là official KMS; `llm-wiki/` là workspace observation.
- `llm-wiki/` nên lưu source map, codebase map, module notes, assumptions, open questions, contradictions và lessons learned.
- Observation chỉ được promote sang `docs/` khi có evidence và khi nó trở thành knowledge chính thức.
- Second brain hiệu quả khi nó giảm việc học lại và chỉ ra vùng rủi ro cho agent tiếp theo.

Gate cần đạt: tri thức mới sau mỗi task được đặt đúng chỗ: official decision vào `docs/`, observation vào `llm-wiki/`, contradiction vào contradiction log.

## Nguyên Tắc Chốt

> Đây không phải là "AI viết code thay engineering". Đây là quy trình SDD + KMS + Agentic Workflow giúp tạo phần mềm có thể bàn giao, trong đó mỗi bước đều có source, gate, evidence, diff và human approval.

## Usage

Dùng tài liệu này khi:

- Onboard thành viên mới vào workflow SDD của repo.
- Chuẩn bị roadmap đào tạo hoặc triển khai nội bộ về KMS + Agentic Development.
- Review xem một task đã có đủ artifact trước khi chuyển sang implementation chưa.
- Thiết kế task brief cho agent trước khi yêu cầu sửa code hoặc tài liệu.
- Phân loại task/risk và xác định escalation path.

Không dùng tài liệu này để:

- Chốt business logic cho project mới khi domain được xác định.
- Xác định validation command cuối cùng.
- Override `AGENTS.md`, grounding manifest, application spec, QA matrix, runbook hoặc model-local policy.
- Cho phép agent tự quyết định business, architecture, security hoặc release.

## Verification

Checklist kiểm tài liệu này:

- Có đúng một H1.
- Có frontmatter hợp lệ với `type`, `status`, `source_of_truth`, `related`.
- Các path nội bộ trong bảng phải tồn tại hoặc được nêu rõ là gap.
- Không tạo business rule mới ngoài nguồn đã đọc.
- Không claim validation/report generation pass.
- Giữ vai trò là methodology/onboarding guide, không trở thành contract hoặc runbook chính thức.

## Traceability Notes

- `docs/04-spec-test/ui-spec.md` hiện chưa có trong repository và không được liệt kê như active source.
- `docs/04-spec-test/10.openapi.yaml` tồn tại nhưng theo manifest hiện là OpenAPI guidance draft/reference, không phải active HTTP runtime contract của repo.
- `docs/04-spec-test/application-spec.md` là canonical starter application spec placeholder; `specs/schemas/tools/application.schema.json` là reusable machine-readable schema placeholder. Concrete project behavior chỉ được thêm khi có requirement, architecture decision, hoặc executable/spec evidence.
- Tài liệu này được catalog trong grounding manifest như training/onboarding methodology source; nếu nội dung đổi thành policy bắt buộc mới, cần cập nhật `AGENTS.md` hoặc artifact policy tương ứng thay vì chỉ sửa tài liệu này.

## Related

- [methodology.md](./methodology.md)
- [d_2.idea_to_spec_before_design.md](./d_2.idea_to_spec_before_design.md)
- [d_3.document_strategy_and_creation_order.md](./d_3.document_strategy_and_creation_order.md)
- [d_1.quickstarts.md](./d_1.quickstarts.md)
- [d_5.agentic_env_setup.md](./d_5.agentic_env_setup.md)
- [o_2.kms_intergrate.md](./o_2.kms_intergrate.md)
- [o_3.kms_obsidian_rg_runbook.md](./o_3.kms_obsidian_rg_runbook.md)
- [o_4.code_intelligence_agent_v2.md](./o_4.code_intelligence_agent_v2.md)
- [o_5.agent_verification_model_v2_reviewed.md](./o_5.agent_verification_model_v2_reviewed.md)
- [agent_request_lifecycle_kms_sdd_grounding.md](./agent_request_lifecycle_kms_sdd_grounding.md)
- [manifest.md](../grounding/manifest.md)
- [sources.md](../grounding/sources.md)
- [prompts.md](../grounding/prompts.md)
- [03.vision.md](../02-architecture/03.vision.md)
- [02.glossary.md](../02-architecture/02.glossary.md)
- [09.bpmn.md](../03-business-analysis/09.bpmn.md)
- [application-spec.md](../04-spec-test/application-spec.md)
- [13.prompt-guardrails.md](../06-quality-assurance/13.prompt-guardrails.md)
- [14.qa-matrix.md](../06-quality-assurance/14.qa-matrix.md)
- [16.review-checklists.md](../07-ci-cd-review/16.review-checklists.md)
- [17.dor.md](../09-runbook/17.dor.md)
- [18.dod.md](../09-runbook/18.dod.md)

## References

- [AGENTS.md](../../AGENTS.md)
- [docs/AGENTS.md](../AGENTS.md)
- [manifest.yaml](../grounding/manifest.yaml)
- [manifest.md](../grounding/manifest.md)
- [sources.md](../grounding/sources.md)
- [prompts.md](../grounding/prompts.md)
- [methodology.md](./methodology.md)
- [d_1.quickstarts.md](./d_1.quickstarts.md)
- [d_3.document_strategy_and_creation_order.md](./d_3.document_strategy_and_creation_order.md)
- [d_5.agentic_env_setup.md](./d_5.agentic_env_setup.md)
- [o_2.kms_intergrate.md](./o_2.kms_intergrate.md)
- [o_3.kms_obsidian_rg_runbook.md](./o_3.kms_obsidian_rg_runbook.md)
- [o_4.code_intelligence_agent_v2.md](./o_4.code_intelligence_agent_v2.md)
- [o_5.agent_verification_model_v2_reviewed.md](./o_5.agent_verification_model_v2_reviewed.md)
- [agent_request_lifecycle_kms_sdd_grounding.md](./agent_request_lifecycle_kms_sdd_grounding.md)
- [13.prompt-guardrails.md](../06-quality-assurance/13.prompt-guardrails.md)
- [14.qa-matrix.md](../06-quality-assurance/14.qa-matrix.md)
- [16.review-checklists.md](../07-ci-cd-review/16.review-checklists.md)

External links preserved from the original note for optional background reading:

- [Kiro specs](https://kiro.dev/docs/specs/)
- [Kiro requirements-first](https://kiro.dev/docs/specs/feature-specs/requirements-first/)
- [Second brain example](https://github.com/henrydaum/second-brain)

## Open Questions

- Có cần tạo artifact riêng cho build-vs-buy/dependency decision trong repo này không, hay dùng ADR hiện có khi có dependency mới?
- Có cần tạo lại MOC navigation active cho methodology/spec-first không, hay tiếp tục dùng `docs/grounding/manifest.md` làm entrypoint?

## Change Notes

- 2026-05-11: Replaced the 17-pillar learning chain with a single 21-pillar operating chain and added roadmap implementation challenges, role levels, and training stages.
- 2026-05-11: Removed presentation-oriented sections and replaced them with a roadmap for learning and implementing SDD end to end.
- 2026-05-11: Normalized metadata, source links, artifact paths, gate model, usage boundaries, and traceability notes. Original learning-path intent preserved.
- 2026-05-11: Removed review-history content, kept the official knowledge-chain structure, and added methodology back-reference targets.
- 2026-05-11: Reworked for operations training after review: added learning levels, review appraisal, task/risk classification, build-vs-buy, AI literacy, Git/diff, testing/RCA, security, human approval, CI/CD rollback, and training checklists.
