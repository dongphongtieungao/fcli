---
id: kms-taxonomy
title: KMS Taxonomy
type: governance
category: governance
status: published
owner: governance
domain: agentic-sdlc
project: current
workspace: docs
created: '2026-04-28'
updated: 2026-06-22
sensitivity: internal
truth_level: official
source_policy: source_of_truth
source_of_truth:
  - docs/grounding/manifest.md
  - docs/methodology/o_2.kms_intergrate.md
  - docs/methodology/o_3.kms_obsidian_rg_runbook.md
  - docs/00-governance/metadata-schema.md
  - docs/00-governance/taxonomy.md
  - docs/01-requirements/00.requirement.md
related:
  - docs/00-governance/metadata-schema.md
  - docs/grounding/manifest.md
  - docs/_templates/doc-template.md
  - docs/06-quality-assurance/14.qa-matrix.md
  - docs/07-ci-cd-review/16.review-checklists.md
  - docs/09-runbook/18.dod.md
upstream:
  - docs/grounding/manifest.md
downstream:
  - docs/00-governance/metadata-schema.md
  - docs/_templates/doc-template.md
  - docs/_templates/llm-wiki-template.md
tags:
  - kms/metadata
  - kms/taxonomy
  - kms/obsidian
  - kms/rg
  - sdd/reusable
  - sdd/generic
---

# KMS Taxonomy

## Purpose

Define controlled values used by Obsidian Properties, Markdown templates, `rg` retrieval, and the reusable KMS/SDD workflow.

## Context

This taxonomy follows the KMS logic in [o_2.kms_intergrate.md](../methodology/o_2.kms_intergrate.md), the operational setup in [o_3.kms_obsidian_rg_runbook.md](../methodology/o_3.kms_obsidian_rg_runbook.md), and the starter repository grounding registry.

Use this file to choose metadata for reusable software-delivery artifacts. Project-specific values should be added only after the new project defines real requirements, architecture, contracts, tests, or operations.

## Category Values For Docs

- `grounding`
- `governance`
- `methodology`
- `requirement`
- `architecture`
- `business_analysis`
- `spec_test`
- `quality_assurance`
- `ci_cd_review`
- `runbook`
- `operation`
- `prompt_governance`

## Category Values For llm-wiki

- `agent_workspace`
- `workspace_index`
- `workspace_memory`
- `workspace_source_map`
- `workspace_codebase`
- `workspace_module`
- `workspace_question`
- `workspace_assumption`
- `workspace_contradiction`
- `workspace_risk`
- `workspace_lesson`
- `workspace_verification`

## Type Values For Docs

- `methodology`
- `guide`
- `technical_note`
- `governance`
- `manifest`
- `source_catalog`
- `prompt_governance`
- `vision`
- `glossary`
- `requirement`
- `use_case`
- `architecture_c4`
- `nfr`
- `adr`
- `standards`
- `bpmn`
- `executable_spec`
- `contract_spec`
- `openapi`
- `application_spec`
- `ui_spec`
- `prompt_guardrails`
- `qa_matrix`
- `quality_matrix`
- `review_checklist`
- `dor`
- `dod`
- `runbook`

## Type Values For llm-wiki

- `workspace_index`
- `source_map`
- `project_memory`
- `codebase_map`
- `module_note`
- `task_memory`
- `open_questions`
- `assumptions`
- `contradiction_log`
- `risk_log`
- `lessons_learned`
- `verification_notes`
- `agent_runbook`

## Status Values For Docs

- `draft`
- `reviewed`
- `published`
- `deprecated`

## Status Guidance

Use `published` for active reusable framework documents, registries, prompt contracts, governance rules, QA gates, and runbooks that are safe to apply across future software projects.

Use `draft` for templates, placeholders, product contracts that still need project-specific evidence, and task-specific notes that have not been promoted.

## Status Values For llm-wiki

- `observed`
- `synthesized`
- `verified`
- `promoted`
- `stale`
- `discarded`

## Truth Level Values

- `official`
- `working`
- `observed`
- `assumption`
- `archived`

## Source Policy Values

- `source_of_truth`
- `source_catalog`
- `machine_registry`
- `methodology`
- `prompt_governance`
- `workspace_only`
- `reference_only`

## Controlled Domain Values

Use `privategpt-adapter` for artifacts that define, implement, verify, or operate the PrivateGPT Adapter MVP. This value is valid only because the canonical requirement has been published.

Existing generic values, including `agentic-sdlc`, `quality`, `delivery`, and `unknown`, remain valid where they accurately describe the artifact.

## Tag Namespaces

- `kms/*`
- `sdd/*`
- `agent/*`
- `workspace/*`
- `risk/*`
- `status/*`

## Reusable Starter Tags

- `sdd/reusable`
- `sdd/generic`
- `sdd/template`
- `sdd/application-spec`
- `sdd/contract`
- `prompt/governance`
- `kms/governance`
- `kms/grounding`

## PrivateGPT Adapter Tag Extension

Use the following controlled tags for the published PrivateGPT Adapter knowledge set. Apply only tags that describe the artifact; do not add a tag merely because a term appears in the text.

- `domain/privategpt-adapter`
- `integration/opencode`
- `integration/privategpt`
- `architecture/provider-spi`
- `contract/openai-compatible`
- `runtime/agent-mode`
- `protocol/sse`
- `security/browser-session`
- `model/gemini-2.5-pro`

## Publication Rule For Project-Specific Documents

A project-specific Markdown document may use `status: published` only when it has a canonical source, a defined scope, a traceable related artifact, and no unresolved contradiction that changes its stated behavior. Use `truth_level: official` and `source_policy: source_of_truth` for a primary requirement or approved appendix; use `reference_only` for observed code or exported vendor documentation.

## Related

- [metadata-schema.md](./metadata-schema.md)
- [manifest.md](../grounding/manifest.md)
- [sources.md](../grounding/sources.md)
- [o_2.kms_intergrate.md](../methodology/o_2.kms_intergrate.md)

## Change Notes

- 2026-04-28: Aligned controlled values with `o_2.kms_intergrate.md` and Obsidian property seed generation.
- 2026-05-14: Reframed taxonomy for reusable generic SDD software projects, added `contract_spec`, and documented reusable starter tags.
- 2026-06-22: Added controlled domain and tag values for the published PrivateGPT Adapter knowledge set.
