---
type: source_catalog
category: grounding
status: published
owner: unknown
domain: grounding
project: fcli
created: 2026-05-13
updated: 2026-06-23
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/manifest.yaml
related:
  - manifest.md
  - prompts.md
  - ../README.md
tags:
  - kms/grounding
  - sdd/reusable
  - sdd/generic
---

# Grounding Sources

## Purpose

Catalog the starter repository sources that agents may cite or consult.

## Context

The catalog is domain-neutral, not empty. It should preserve reusable source IDs for the starter repository so agents can route work without reviving old project assumptions. Add domain-specific sources only when the new project has confirmed requirements, architecture, contracts, tests, or runbooks.

## Source Catalog

| Source ID | Path | Type | Owner | Coverage | Truth level | Usage rule |
|---|---|---|---|---|---|---|
| `root-agent-policy` | `AGENTS.md` | agent_policy | unknown | Repository-wide agent contract | official | Read before code or docs changes |
| `docs-agent-policy` | `docs/AGENTS.md` | agent_policy | unknown | Documentation KMS contract | official | Read before editing `docs/` |
| `kms-metadata-schema` | `docs/00-governance/metadata-schema.md` | governance | governance | Metadata and frontmatter rules | official | Use when normalizing Markdown metadata |
| `kms-taxonomy` | `docs/00-governance/taxonomy.md` | governance | governance | Controlled values and document types | official | Use when choosing document type, status, truth level, or tags |
| `data-governance-baseline` | `docs/00-governance/00.data-governance.md` | governance | unknown | Data classification, retention, quality, access, auditability | official | Use when data sources or data controls are introduced |
| `governance-rules-baseline` | `docs/00-governance/00.governance-rules.md` | governance | unknown | Reusable repository governance | official | Use when changing source-of-truth or repository controls |
| `patterns-anti-patterns-baseline` | `docs/00-governance/00.patterns-anti-patterns.md` | governance | unknown | Reusable practices and cleanup anti-patterns | official | Use when reviewing methodology or cleanup behavior |
| `requirements-index` | `docs/01-requirements/00.requirement.md` | requirement | unknown | Published OpenCode PrivateGPT Bridge requirement (compatibility alias) | official | Use as the primary requirement index for OpenCode PrivateGPT Bridge work |
| `privategpt-adapter-requirement` | `docs/01-requirements/00.requirement.md` | requirement | unknown | Canonical OpenCode PrivateGPT Bridge MVP requirement | official | Use as the authoritative requirement for OpenCode PrivateGPT Bridge implementation |
| `privategpt-adapter-architecture-design` | `docs/01-requirements/00.achieteture_idea_v3.md` | architecture | unknown | Published architecture-design appendix for the canonical requirement | official | Use for the published local Python bridge architecture rationale and design boundaries; the parent requirement remains authoritative for scope. |
| `provider-spi-spec` | `docs/01-requirements/00.requirement_Provider_SPI_Specification.md` | contract_spec | unknown | Published Provider SPI appendix | official | Use when implementing Provider SPI adapters or validating provider boundaries |
| `privategpt-adapter-arch-v2` | `docs/01-requirements/00.requirement_PrivateGPT_Adapter_Architecture_v2.md` | architecture | unknown | Published OpenCode PrivateGPT Bridge architecture appendix | official | Use when planning or validating the adapter architecture and sync rules |
| `business-requirements` | `docs/01-requirements/00.final-business-requirements.md` | requirement | unknown | Published OpenCode PrivateGPT Bridge MVP business requirements | official | Use as the business-facing scope and acceptance source for OpenCode PrivateGPT Bridge work |
| `use-cases` | `docs/01-requirements/01.use-cases.md` | use_case | unknown | Use cases and acceptance drafts | official | Use when mapping actors, flows, and acceptance criteria |
| `architecture-glossary` | `docs/02-architecture/02.glossary.md` | glossary | unknown | Published OpenCode PrivateGPT Bridge technical terminology and boundaries | official | Use when terminology, integration boundaries, or technical contracts change |
| `architecture-vision` | `docs/02-architecture/03.vision.md` | vision | unknown | Goals, non-goals, scope | official | Use when project direction needs grounding |
| `architecture-c4` | `docs/02-architecture/04.architecture-c4.md` | architecture_c4 | unknown | Published C4 boundaries for the local Python bridge | official | Use when changing actors, containers, components, dependency direction, Agent binding or streaming flow |
| `architecture-nfr` | `docs/02-architecture/05.nfr.md` | nfr | unknown | Published MVP quality constraints and verification expectations | official | Use when security, reliability, streaming, performance, operability or maintainability constraints are affected |
| `architecture-adr` | `docs/02-architecture/06.adr.md` | adr | unknown | Published decision register for the MVP architecture | official | Use when a boundary, deployment, provider, Agent, resilience or credential decision changes |
| `architecture-standards` | `docs/02-architecture/08.standards.md` | standards | unknown | Published Python, security, contract and verification standards | official | Use when coding, review, architecture or documentation standards are affected |
| `business-glossary` | `docs/03-business-analysis/02.glossary.md` | glossary | unknown | Published OpenCode PrivateGPT Bridge actor and acceptance terminology | official | Use when business terminology, pilot scope, or acceptance wording changes |
| `privategpt-adapter-business-glossary` | `docs/03-business-analysis/02.glossary.md` | glossary | unknown | OpenCode PrivateGPT Bridge business glossary alias | official | Use when mapping the canonical requirement to actors, pilot behavior, or acceptance criteria |
| `business-bpmn` | `docs/03-business-analysis/09.bpmn.md` | bpmn | unknown | Published BPMN for Bridge startup, login, Agent sync and chat completion | official | Use when analyzing business flow, BPMN gateways, process exceptions or acceptance scenarios for the Bridge MVP |
| `privategpt-adapter-bdd-acceptance` | `tests/features/*.feature`, `tests/bdd/` | executable_spec | unknown | Gherkin acceptance specifications and BPMN-to-BDD traceability for the Bridge MVP | official | Use when deriving, implementing, reviewing, or verifying acceptance behavior; the canonical requirement and BPMN remain the source for business scope. |
| `spec-jsonschema-governance` | `docs/04-spec-test/11.governance-jsonschema-rules.md` | contract_spec | unknown | JSON Schema governance | official | Use when schema validation rules are introduced |
| `spec-spectral-governance` | `docs/04-spec-test/12.governance-spectral.md` | contract_spec | unknown | OpenAPI/API lint governance | official | Use when API lint rules are introduced |
| `prompt-guardrails` | `docs/06-quality-assurance/13.prompt-guardrails.md` | prompt_guardrails | unknown | AI scope, safety, tool/file, contract-gap and evidence guardrails for the Bridge | official | Read before AI planning, implementation or review that can affect Bridge behavior or contracts. |
| `qa-matrix` | `docs/06-quality-assurance/14.qa-matrix.md` | qa_matrix | unknown | Functional/NFR checks mapped from requirement and spec to required evidence | official | Use to select affected QA rows, fixtures, test level and evidence before implementation or verification. |
| `quality-matrix` | `docs/07-ci-cd-review/15.quality-matrix.md` | quality_matrix | unknown | CI/PR quality gates, change-type mapping and blocking rules | official | Use to decide which automated/manual gates must pass before merge or handover. |
| `review-checklists` | `docs/07-ci-cd-review/16.review-checklists.md` | review_checklist | unknown | PR/design review criteria linked to QA and quality-gate IDs | official | Use for Bridge PR, contract, security, streaming, file-context and release review. |
| `definition-of-ready` | `docs/09-runbook/17.dor.md` | dor | unknown | Task-packet and readiness criteria for independent planner/coder/tester/verifier execution | official | Use before implementation to resolve scope, sources, scenarios, PG/QA/QG IDs, gaps and expected evidence. |
| `definition-of-done` | `docs/09-runbook/18.dod.md` | dod | unknown | Evidence, review, KMS and handover completion contract | official | Use before merge, release or handover to assign a scope-qualified verdict and assemble the reusable handover packet. |
| `release-case-study` | `docs/10-release/sdd-case-study-report.md` | technical_note | unknown | Release and case-study evidence outline | draft | Use for release or case-study evidence planning |
| `grounding-manifest-human` | `docs/grounding/manifest.md` | manifest | unknown | Human-readable grounding map | official | Use as grounding entrypoint |
| `grounding-manifest-machine` | `docs/grounding/manifest.yaml` | manifest | unknown | Machine-readable source registry | official | Use for source IDs and coverage |
| `grounding-sources-catalog` | `docs/grounding/sources.md` | source_catalog | unknown | Source IDs and usage rules | official | Use when citing sources |
| `prompt-governance-routing` | `docs/grounding/prompts.md` | prompt_governance | unknown | Prompt lanes and routing | official | Use when choosing work lane |
| `prompt-application-architect` | `docs/grounding/prompts/prompt-application-architect.md` | prompt_governance | prompt-governance | Planning and architecture prompt | official | Use before non-trivial implementation planning |
| `prompt-application-coder` | `docs/grounding/prompts/prompt-application-coder.md` | prompt_governance | prompt-governance | Implementation prompt | official | Use after grounding and scope are clear |
| `prompt-application-debugger` | `docs/grounding/prompts/prompt-application-debugger.md` | prompt_governance | prompt-governance | Debugging and minimal fix prompt | official | Use when reproducible failure evidence exists |
| `prompt-application-interface` | `docs/grounding/prompts/application-interface.md` | prompt_governance | prompt-governance | Application interface design | official | Use for API, UI, job, library, local-tool, or workflow interface design |
| `prompt-application-config` | `docs/grounding/prompts/application-config.md` | prompt_governance | prompt-governance | Configuration design and precedence | official | Use when settings, defaults, validation, or secret-provider behavior are affected |
| `prompt-application-runtime` | `docs/grounding/prompts/application-runtime.md` | prompt_governance | prompt-governance | Runtime workflow and failure modes | official | Use when runtime flow, routing, outputs, or operational behavior are affected |
| `prompt-application-logging` | `docs/grounding/prompts/application-logging.md` | prompt_governance | prompt-governance | Logging and redaction | official | Use when logs, notifications, redaction, or observability behavior are affected |
| `prompt-application-validation` | `docs/grounding/prompts/application-validation.md` | prompt_governance | prompt-governance | Validation and evidence | official | Use when verification status, warnings, artifacts, or validation checks are affected |
| `grounding-evals` | `docs/grounding/evals.md` | technical_note | unknown | Grounding quality checks | official | Use when defining grounding evaluation |
| `task-workspace-methodology` | `docs/methodology/Task.md` | methodology | governance | Task workspace lifecycle, profiles, promotion | official | Use for `llm-wiki/tasks/current/` |
| `docs-readme` | `docs/README.md` | guide | unknown | Documentation index | official | Use for docs orientation |
| `starter-readme` | `README.md` | guide | unknown | Repository onboarding | official | Use for repository orientation |
| `antigravity-agent-bridge` | `GEMINI.md` | agent_policy | unknown | Antigravity/Gemini-facing bridge | official | Read after `AGENTS.md` when Antigravity is the active agent |
| `antigravity-playbook` | `docs/antigravity/playbook.md` | runbook | unknown | Antigravity adapter workflow | official | Use when running Antigravity against the reusable SDD/KMS workflow |
| `antigravity-sdd-rule` | `.agent/rules/sdd-antigravity.md` | agent_policy | unknown | Antigravity grounding, verification, and sync rule | official | Use as the Antigravity operating rule |
| `plugin-code` | `docs/01-requirements/plugin-code` | reference_code | unknown | Java plugin reference implementation (evidence) | draft | Use when mapping internal API behavior from plugin |
| `opencode-cli-doc` | `docs/01-requirements/opencode-cli-doc` | guide | unknown | OpenCode CLI usage and integration guide | draft | Use when integrating adapter with OpenCode CLI |
| `shared-skill-sdd-request-intake` | `.agents/skills/sdd-request-intake/SKILL.md` | guide | unknown | SDD request intake adapter (shared) | official | Adapter for `docs/grounding/skills/1.sdd-request-intake.md` |
| `shared-skill-kms-retrieval-context-pack` | `.agents/skills/kms-retrieval-context-pack/SKILL.md` | guide | unknown | KMS context retrieval adapter (shared) | official | Adapter for `docs/grounding/skills/2.kms-retrieval-context-pack.md` |
| `shared-skill-prompt-lane-router` | `.agents/skills/prompt-lane-router/SKILL.md` | guide | unknown | Prompt lane router adapter (shared) | official | Adapter for `docs/grounding/skills/3.prompt-lane-router.md` |
| `shared-skill-spec-acceptance-mapping` | `.agents/skills/spec-acceptance-mapping/SKILL.md` | guide | unknown | Acceptance mapping adapter (shared) | official | Adapter for `docs/grounding/skills/4.spec-acceptance-mapping.md` |
| `shared-skill-code-boundary-file-role` | `.agents/skills/code-boundary-file-role/SKILL.md` | guide | unknown | Code boundary adapter (shared) | official | Adapter for `docs/grounding/skills/5.code-boundary-file-role.md` |
| `shared-skill-evidence-verification` | `.agents/skills/evidence-verification/SKILL.md` | guide | unknown | Evidence verification adapter (shared) | official | Adapter for `docs/grounding/skills/6.evidence-verification.md` |
| `shared-skill-kms-sync-drift-control` | `.agents/skills/kms-sync-drift-control/SKILL.md` | guide | unknown | KMS sync/drift adapter (shared) | official | Adapter for `docs/grounding/skills/7.kms-sync-drift-control.md` |
| `shared-skill-archive-current-task` | `.agents/skills/archive-current-task/SKILL.md` | guide | unknown | Task archive adapter (shared) | official | Adapter for `docs/grounding/skills/8.archive-current-task.md` |
| `shared-skill-create-current-task` | `.agents/skills/create-current-task/SKILL.md` | guide | unknown | Task creation adapter (shared) | official | Adapter for `docs/grounding/skills/9.create-current-task.md` |
| `skill-plan-task` | `docs/grounding/skills/10.plan-task.md` | methodology | governance | SDD planning orchestrator | official | Use before non-trivial implementation to build a structured plan |
| `privategpt-adapter-openapi` | `specs/10.openapi.yaml` | openapi | unknown | Draft OpenAI-compatible local Bridge API contract for OpenCode integration | draft | Use for local Bridge implementation and contract tests; do not claim complete wire compatibility until the documented runtime-validation gaps are resolved. |
| `privategpt-adapter-application-spec` | `docs/04-spec-test/application-spec.md` | application_spec | unknown | Draft application contract for Bridge CLI, local API and runtime flows | draft | Use when implementing or verifying Bridge lifecycle, configuration, auth, Agent sync, chat/SSE, error, file-context and observability behavior. |
| `starter-application-spec` | `docs/04-spec-test/application-spec.md` | application_spec | unknown | Compatibility alias for the current project application specification | draft | Resolve to `privategpt-adapter-application-spec`; retained for generic prompt references. |
| `starter-application-schema` | `specs/schemas/tools/application.schema.json` | application_spec | unknown | Starter application/tool schema placeholder | draft | Use only after project interfaces or operations are defined |

## Source Group Rules

| Group | Use when | Notes |
|---|---|---|
| Governance | Metadata, taxonomy, data controls, or source-of-truth handling changes | Must remain linked to manifest and sources |
| Requirements | Requirement analysis, scope, use cases, or acceptance drafts | Do not invent business rules without source/user confirmation |
| Architecture | Boundary, NFR, ADR, standards, or terminology changes | Prefer patching existing canonical docs over creating parallel docs |
| Business analysis | Workflow, state, actor, happy path, or error path changes | Link requirement and acceptance evidence when available |
| Specs/contracts | API, application, schema, UI, or output contract changes | Update contract and verification expectations together |
| Quality/review | Test strategy, QA matrix, review checklist, or guardrails change | Record exact checks and limitations |
| Runbook/release | Install, package, release, rollback, handover, or evidence pack changes | Do not claim release-ready without evidence |
| Antigravity | Agent bridge, rules, adapter skills, and Antigravity playbook changes | Keep adapter docs linked to canonical SDD guides |
| Workspace | Temporary findings, assumptions, and task memory | Never overrides official docs |

## Related

- [manifest.md](./manifest.md)
- [manifest.yaml](./manifest.yaml)
- [prompts.md](./prompts.md)
- [docs README](../README.md)
