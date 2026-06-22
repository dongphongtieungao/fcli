---
type: manifest
category: grounding
status: published
owner: unknown
domain: grounding
project: ftransform
created: 2026-05-13
updated: 2026-06-22
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/AGENTS.md
  - docs/grounding/manifest.yaml
  - docs/grounding/sources.md
  - docs/01-requirements/00.requirement.md
  - docs/01-requirements/00.requirement_Provider_SPI_Specification.md
  - docs/01-requirements/00.requirement_PrivateGPT_Adapter_Architecture_v2.md
  - docs/01-requirements/plugin-code
  - docs/01-requirements/opencode-cli-doc
related:
  - sources.md
  - prompts.md
  - ../README.md
  - ../methodology/Task.md
tags:
  - kms/grounding
  - sdd/reusable
  - sdd/generic
---

# Grounding Manifest

## Purpose

Provide the human-readable grounding entrypoint for the starter repository.

## Context

This manifest does not describe the removed legacy application domain. It records the reusable source-of-truth layers that should exist in a new project: governance, methodology, requirements, architecture, business analysis, contracts, quality, runbook, release, workspace memory, and prompt governance.

Use [manifest.yaml](./manifest.yaml) as the machine-readable registry and [sources.md](./sources.md) as the citation catalog. This file explains the registry for humans.

## Precedence

| Priority | Source | Role |
|---|---|---|
| 1 | Current user task | Immediate task intent |
| 2 | `AGENTS.md` | Root agent operating contract |
| 3 | Local `AGENTS.md` files | Path-specific refinements |
| 4 | `docs/grounding/manifest.yaml` | Machine-readable source registry |
| 5 | `docs/grounding/sources.md` | Source catalog |
| 6 | `docs/grounding/prompts.md` | Prompt lane routing |
| 7 | Other official docs | Project knowledge |
| 8 | Tests/specs | Executable behavior |
| 9 | `llm-wiki/` | Workspace memory |
| 10 | `src/` | Current implementation |

## Source Groups

| Group | Representative paths | Registry role |
|---|---|---|
| Agent policy | `AGENTS.md`, `docs/AGENTS.md` | Defines agent behavior, safety, and documentation rules |
| Governance | `docs/00-governance/` | Defines metadata, taxonomy, data governance, and reusable KMS rules |
| Requirements | `docs/01-requirements/` | Holds product requirements, use cases, and acceptance drafts |
| Architecture | `docs/02-architecture/` | Holds glossary, vision, C4, NFR, ADR, standards, and structure |
| Business analysis | `docs/03-business-analysis/` | Holds business glossary, BPMN, BA notes, and workflow analysis |
| Specs and contracts | `docs/04-spec-test/`, `specs/` | Holds OpenAPI, application, schema, and lint governance |
| Environment | `docs/05-environment-setup/` | Holds setup guidance and environment templates |
| Quality | `docs/06-quality-assurance/`, `docs/07-ci-cd-review/` | Holds QA matrix, prompt guardrails, review checklist, and quality matrix |
| Runbook | `docs/09-runbook/` | Holds DoR, DoD, operating, install, rollback, and handover guidance |
| Release | `docs/10-release/` | Holds release notes, evidence packs, and case-study/handover material |
| Methodology | `docs/methodology/` | Holds Spec First, KMS, task lifecycle, and verification workflows |
| Grounding | `docs/grounding/` | Holds manifest, source catalog, prompts, evals, and skill docs |
| Workspace memory | `llm-wiki/` | Holds non-authoritative observations, task memory, and verification notes |
| Starter contracts | `docs/04-spec-test/`, `specs/` | Holds starter application/API/schema contract placeholders |

## Core Source IDs

| Source ID | Path | Role |
|---|---|---|
| `root-agent-policy` | `AGENTS.md` | Root agent operating contract |
| `docs-agent-policy` | `docs/AGENTS.md` | Documentation KMS contract |
| `kms-metadata-schema` | `docs/00-governance/metadata-schema.md` | Metadata rules |
| `kms-taxonomy` | `docs/00-governance/taxonomy.md` | Controlled values |
| `data-governance-baseline` | `docs/00-governance/00.data-governance.md` | Data governance baseline |
| `grounding-manifest-human` | `docs/grounding/manifest.md` | Human grounding map |
| `grounding-manifest-machine` | `docs/grounding/manifest.yaml` | Machine-readable registry |
| `grounding-sources-catalog` | `docs/grounding/sources.md` | Citation catalog |
| `prompt-governance-routing` | `docs/grounding/prompts.md` | Prompt lane routing |
| `task-workspace-methodology` | `docs/methodology/Task.md` | Task workspace lifecycle |
| `definition-of-ready` | `docs/09-runbook/17.dor.md` | Ready gate |
| `definition-of-done` | `docs/09-runbook/18.dod.md` | Done evidence gate |
| `qa-matrix` | `docs/06-quality-assurance/14.qa-matrix.md` | Verification expectations |
| `starter-application-spec` | `docs/04-spec-test/application-spec.md` | Starter application interface placeholder |
| `starter-application-schema` | `specs/schemas/tools/application.schema.json` | Starter application/tool schema placeholder |
| `architecture-glossary` | `docs/02-architecture/02.glossary.md` | Published PrivateGPT Adapter technical glossary |
| `privategpt-adapter-business-glossary` | `docs/03-business-analysis/02.glossary.md` | Published PrivateGPT Adapter business glossary |
| `privategpt-adapter-requirement` | `docs/01-requirements/00.requirement.md` | Published canonical PrivateGPT Adapter requirement |
| `provider-spi-spec` | `docs/01-requirements/00.requirement_Provider_SPI_Specification.md` | Published Provider SPI appendix |
| `privategpt-adapter-arch-v2` | `docs/01-requirements/00.requirement_PrivateGPT_Adapter_Architecture_v2.md` | Published architecture appendix |
| `plugin-code` | `docs/01-requirements/plugin-code` | Java plugin reference code |
| `opencode-cli-doc` | `docs/01-requirements/opencode-cli-doc` | OpenCode CLI user guide |

## Usage By Task Type

| Task type | Read first | Then read |
|---|---|---|
| Documentation/KMS change | `docs/AGENTS.md`, this manifest, `sources.md` | Target artifact and related methodology |
| Requirement analysis | Requirements, glossary, BPMN, `Task.md` | DoR, QA matrix, relevant task history |
| Business behavior change | Requirement/use case, BPMN, contract/spec | Code boundary, QA matrix, DoD |
| Code implementation | Root policy, manifest, task-specific source IDs | Interfaces/contracts/tests/runbook relevant to the change |
| Package/release | DoR, DoD, QA matrix, runbook, release docs | Verification notes and artifact evidence |
| Prompt/agent workflow change | `prompts.md`, prompt files, task lifecycle docs | Manifest and source catalog |

## Update Rule

When a new official artifact becomes canonical:

1. Add or update its `source_id` in [manifest.yaml](./manifest.yaml).
2. Add or update its row in [sources.md](./sources.md).
3. Link it from the related artifact layer.
4. Record whether prompt routing, QA, runbook, or methodology needs a matching update.
5. Do not add old workspace notes as official sources unless they have been reviewed and promoted.

## Related

- [sources.md](./sources.md)
- [prompts.md](./prompts.md)
- [metadata-schema.md](../00-governance/metadata-schema.md)
- [taxonomy.md](../00-governance/taxonomy.md)
- [docs README](../README.md)
- [Task.md](../methodology/Task.md)
