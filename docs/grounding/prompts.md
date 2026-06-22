---
id: grounding-prompts
title: Prompt Pack - Starter Prompt Governance
type: prompt_governance
category: grounding
status: published
owner: prompt-governance
domain: prompt_governance
project: fcli
workspace: docs
created: 2026-04-27
updated: 2026-05-14
sensitivity: internal
truth_level: official
source_policy: prompt_governance
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.yaml
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
  - docs/grounding/skills
related:
  - manifest.md
  - manifest.yaml
  - sources.md
  - ../antigravity/playbook.md
  - ../../GEMINI.md
  - prompts/prompt-application-architect.md
  - prompts/prompt-application-coder.md
  - prompts/prompt-application-debugger.md
  - prompts/prompt-grounding-update.md
tags:
  - kms/grounding
  - prompt/governance
  - sdd/reusable
  - sdd/generic
---

# Prompt Pack - Starter Prompt Governance

## Purpose

Tài liệu này chuẩn hóa bộ prompt dùng cho agentic development trong repo starter.

Vai trò của nó là:

- giải thích prompt nào dùng cho lane nào;
- ràng buộc prompt vào grounding pack và `AGENTS.md`;
- giúp agent đi đúng flow `grounding -> spec -> code -> verify -> sync docs`;
- giữ prompt governance reusable cho project mới mà không kéo theo domain cũ.

## Context

Repo hiện chưa có domain chính thức cho project mới. Vì vậy file này giữ các lane chung như planning, implementation, debugging, testing, documentation, KMS maintenance, và workspace update. Các lane module-specific chỉ được thêm khi project mới có module hoặc workflow bền vững.

## Nguyên Tắc Chung

Mọi prompt trong `docs/grounding/prompts/` phải tuân thủ:

1. Bám `AGENTS.md` trước, prompt sau.
2. Dùng `docs/grounding/manifest.yaml` làm source registry khi cần source ID.
3. Không tự tạo business rule khi chưa có grounding.
4. Tôn trọng ranh giới module hoặc bounded context.
5. Không overclaim current-state. Nếu repo chưa có CI, runtime, secret scanning automation, hoặc test harness thì prompt không được mô tả chúng như đã implemented.
6. Luôn yêu cầu verification thích hợp với lane được giao.
7. Không hardcode secrets, credentials, PII, hoặc private connection details trong prompt examples.

## Metadata Khuyến Nghị Cho Từng Prompt

Mỗi prompt module-level nên có frontmatter tối thiểu:

```yaml
id: prompt-example
name: Prompt Example
version: 0.1.0
owner: unknown
last_updated: YYYY-MM-DD
intended_use: Explain when this prompt should be used.
references:
  - grounding-manifest-human
  - grounding-sources-catalog
constraints:
  - Follow AGENTS.md first.
  - Do not invent domain rules.
  - Do not claim verification that has not run.
```

## Prompt Set Hiện Tại

### General Orchestration

| File | Vai trò |
|---|---|
| `docs/grounding/prompts/prompt-application-architect.md` | Planning, architecture, scope, and trade-off analysis |
| `docs/grounding/prompts/prompt-application-coder.md` | Implementation prompt for code and tests |
| `docs/grounding/prompts/prompt-application-debugger.md` | Root-cause analysis and minimal fix |
| `docs/grounding/prompts/prompt-grounding-update.md` | Sync manifest, sources, grounding docs, and prompt registry |

### Reusable Runtime / Application Prompts

| File | Vai trò |
|---|---|
| `docs/grounding/prompts/application-interface.md` | Future application interface design |
| `docs/grounding/prompts/application-config.md` | Configuration design and precedence |
| `docs/grounding/prompts/application-runtime.md` | Runtime workflow, routing, outputs, and failure modes |
| `docs/grounding/prompts/application-logging.md` | Logging contract and redaction expectations |
| `docs/grounding/prompts/application-validation.md` | Validation, evidence, warnings, and artifact checks |

### Agentic Workflow

| File | Vai trò |
|---|---|
| `docs/grounding/prompts/agentic_sync_profile.md` | Sync behavior for agentic workflow |

## Operational Skill Pack

| File | Vai trò |
|---|---|
| `docs/grounding/skills/1.sdd-request-intake.md` | Chuẩn hóa request thành task brief có scope, risk, impacted module, và verification expectation |
| `docs/grounding/skills/2.kms-retrieval-context-pack.md` | Dựng context pack bằng `rg` hoặc `tools/kb.ps1` theo source precedence |
| `docs/grounding/skills/3.prompt-lane-router.md` | Chọn lane/prompt contract đã có trong registry, không tự tạo lane mới |
| `docs/grounding/skills/4.spec-acceptance-mapping.md` | Map behavior sang requirement, flow, contract, acceptance criteria, test, và evidence |
| `docs/grounding/skills/5.code-boundary-file-role.md` | Xác định module/file role, caller/callee, local policy, và boundary trước code change |
| `docs/grounding/skills/6.evidence-verification.md` | Chuẩn hóa evidence pack, command resolution, warning/error, và artifact inspection |
| `docs/grounding/skills/7.kms-sync-drift-control.md` | Quyết định sync `llm-wiki`, `docs`, manifest, sources, prompts hoặc không sync |
| `docs/grounding/skills/8.archive-current-task.md` | Archive `llm-wiki/tasks/current/` sang task history |
| `docs/grounding/skills/9.create-current-task.md` | Tạo `llm-wiki/tasks/current/` từ mô tả task mới |
| `docs/grounding/skills/10.plan-task.md` | Orchestrate SDD skills thành plan trước implementation |

## Antigravity Adapter Skill Pack

Antigravity đọc skills từ **cả hai** thư mục `.agents/skills/` và `.agent/skills/`. Do đó, 9 SDD operational skills trong `.agents/skills/` không cần duplicate sang `.agent/skills/`.

**SDD Skills** (shared via `.agents/skills/`, cả Antigravity lẫn Codex đều đọc):

| Adapter path | Canonical source |
|---|---|
| `.agents/skills/sdd-request-intake/SKILL.md` | `docs/grounding/skills/1.sdd-request-intake.md` |
| `.agents/skills/kms-retrieval-context-pack/SKILL.md` | `docs/grounding/skills/2.kms-retrieval-context-pack.md` |
| `.agents/skills/prompt-lane-router/SKILL.md` | `docs/grounding/skills/3.prompt-lane-router.md` |
| `.agents/skills/spec-acceptance-mapping/SKILL.md` | `docs/grounding/skills/4.spec-acceptance-mapping.md` |
| `.agents/skills/code-boundary-file-role/SKILL.md` | `docs/grounding/skills/5.code-boundary-file-role.md` |
| `.agents/skills/evidence-verification/SKILL.md` | `docs/grounding/skills/6.evidence-verification.md` |
| `.agents/skills/kms-sync-drift-control/SKILL.md` | `docs/grounding/skills/7.kms-sync-drift-control.md` |
| `.agents/skills/archive-current-task/SKILL.md` | `docs/grounding/skills/8.archive-current-task.md` |
| `.agents/skills/create-current-task/SKILL.md` | `docs/grounding/skills/9.create-current-task.md` |
| `.agents/skills/plan-task/SKILL.md` | `docs/grounding/skills/10.plan-task.md` |

**Utility Skills** (only in `.agent/skills/`, Antigravity-specific):

| Adapter path | Purpose |
|---|---|
| `.agent/skills/architect-review/SKILL.md` | Architecture review and pattern analysis |
| `.agent/skills/c4-architecture-c4-architecture/SKILL.md` | C4 architecture documentation generation |
| `.agent/skills/cicd-automation-workflow-automate/SKILL.md` | CI/CD pipeline and workflow automation |
| `.agent/skills/code-refactoring-refactor-clean/SKILL.md` | Clean code refactoring |
| `.agent/skills/code-refactoring-tech-debt/SKILL.md` | Technical debt analysis |
| `.agent/skills/code-reviewer/SKILL.md` | Code review and security analysis |
| `.agent/skills/python-performance-optimization/SKILL.md` | Python performance profiling |
| `.agent/skills/python-pro/SKILL.md` | Python 3.12+ best practices |
| `.agent/skills/python-testing-patterns/SKILL.md` | Python testing with pytest |
| `.agent/skills/security-scanning-security-dependencies/SKILL.md` | Dependency vulnerability scanning |

Antigravity orchestration starts from `GEMINI.md`, `.agent/rules/sdd-antigravity.md`, and `docs/antigravity/playbook.md`. Do not add a separate Antigravity prompt lane unless the reusable prompt registry is updated with source IDs and usage rules.

## Khi Nào Dùng Lane Nào

### Documentation Lane

Use this lane for durable Markdown changes under `docs/`, grounding updates, governance docs, runbooks, and templates. Required grounding: `AGENTS.md`, `docs/AGENTS.md`, `docs/grounding/manifest.md`, `docs/grounding/sources.md`, and the relevant methodology document.

### Planning Lane

Use this lane for scope, architecture, trade-off analysis, implementation planning, or acceptance criteria before non-trivial edits. Start with `prompt-application-architect.md`.

### Implementation Lane

Use this lane for code changes after retrieval identifies relevant docs, tests, contracts, and source boundaries. Start with `prompt-application-coder.md`.

### Debugging Lane

Use this lane when a failure needs root-cause analysis. Start with `prompt-application-debugger.md`, identify the smallest reproducible surface, and avoid broad refactors unless explicitly scoped.

### Review Lane

Use this lane for code review, documentation review, KMS review, and traceability review. Findings should cite file paths and grounded sources. Do not silently resolve contradictions; record them in workspace notes when useful.

### Refactoring Lane

Use this lane only after behavior is protected or explicitly scoped. Prefer deletion, reuse, and boundary repair over new abstractions.

### Testing Lane

Use this lane to resolve canonical validation commands before running tests. Do not use examples as canonical commands without checking the source of truth.

### KMS Maintenance Lane

Use this lane when updating `docs/grounding/manifest.md`, `manifest.yaml`, `sources.md`, `prompts.md`, taxonomy, templates, `AGENTS.md`, or `tools/kb.ps1`. The official KMS is `docs/`; `llm-wiki/` remains workspace-only.

### Workspace Update Lane

Use this lane after a task to update `llm-wiki/` with observations, assumptions, contradictions, source maps, module notes, or lessons learned. Workspace notes must not become official unless promoted into `docs/` and cataloged.

## Routing By Work Type

| Work type | Start with | Add when relevant |
|---|---|---|
| Scope or architecture planning | `prompt-application-architect.md` | `spec-acceptance-mapping`, `kms-retrieval-context-pack` |
| Code implementation | `prompt-application-coder.md` | `code-boundary-file-role`, tests/specs |
| Debugging | `prompt-application-debugger.md` | failure logs, targeted reproduction |
| Grounding update | `prompt-grounding-update.md` | `manifest.yaml`, `sources.md`, this file |
| application/config/runtime | `application-interface.md`, `application-config.md`, `application-runtime.md` | `application-validation.md`, specs/runbook |
| Documentation cleanup | `docs/AGENTS.md`, this file | document strategy, manifest/sources |

## Output Contract Cho Prompt Module-Level

Mọi prompt module-level nên ép agent trả lời theo khung này khi task không chỉ là brainstorm:

1. `Phạm vi`
2. `Nguồn grounding đã dùng`
3. `Nhận định hoặc thay đổi đề xuất`
4. `Verification cần chạy hoặc đã chạy`
5. `Rủi ro / điểm chưa đủ bằng chứng`

## Điều Cấm

Prompt không được hướng agent tới các hành vi sau:

- bỏ qua `AGENTS.md`;
- coi `llm-wiki/` là source of truth chính thức;
- mô tả CI, runtime, logging, secret scanning, hoặc test automation như đã implemented nếu repo chưa chứng minh;
- yêu cầu hardcode secrets, credentials, PII, hoặc private connection strings;
- tạo domain-specific lane khi project mới chưa định nghĩa module hoặc workflow bền vững;
- claim verification chưa chạy.

## Quy Tắc Bảo Trì

Khi thêm prompt module-level mới:

1. Tạo file trong `docs/grounding/prompts/`.
2. Thêm source ID vào `docs/grounding/manifest.yaml`.
3. Thêm catalog entry vào `docs/grounding/sources.md`.
4. Nếu prompt đó là lane chính, cập nhật lại file này.

Khi thêm hoặc đổi operational skill trong `docs/grounding/skills/`:

1. Giữ `AGENTS.md` và `docs/AGENTS.md` là policy cao hơn skill.
2. Cập nhật bảng Operational Skill Pack trong file này nếu skill đổi vai trò.
3. Cập nhật `docs/grounding/manifest.md`, `manifest.yaml`, và `sources.md` nếu path, status, owner, coverage hoặc source role đổi.
4. Không tạo prompt lane mới chỉ vì có skill mới; lane vẫn phải được route qua registry/prompt governance.

## Related

- [manifest.md](./manifest.md)
- [manifest.yaml](./manifest.yaml)
- [sources.md](./sources.md)

## Change Notes

- 2026-04-27: Original prompt governance normalized for the previous project.
- 2026-05-13: Reworked as starter prompt governance; preserved reusable governance structure and removed previous project-specific lanes.
