---
id: kms-metadata-schema
title: KMS Metadata Schema
type: governance
category: governance
status: published
owner: governance
domain: agentic-sdlc
project: current
workspace: docs
created: '2026-04-28'
updated: '2026-04-30'
sensitivity: internal
truth_level: official
source_policy: source_of_truth
source_of_truth:
- docs/grounding/manifest.md
- docs/00-governance/taxonomy.md
- docs/methodology/o_2.kms_intergrate.md
- docs/00-governance/metadata-schema.md
related:
- docs/00-governance/taxonomy.md
- docs/_templates/doc-template.md
- docs/_templates/grounding-template.md
- docs/_templates/llm-wiki-template.md
- docs/grounding/manifest.md
upstream:
- docs/grounding/manifest.md
- docs/00-governance/taxonomy.md
downstream:
- docs/_templates/doc-template.md
- docs/_templates/grounding-template.md
- docs/_templates/llm-wiki-template.md
tags:
- kms/metadata
- kms/taxonomy
- kms/obsidian
- risk/performance
---
# KMS Metadata Schema

## Purpose

Define the Obsidian-compatible metadata contract for official `docs/` artifacts and controlled `llm-wiki/` workspace notes.

## Context

This schema supports the lightweight KMS described in [o_2.kms_intergrate.md](../methodology/o_2.kms_intergrate.md). Use [taxonomy.md](./taxonomy.md) for controlled values.

## Required Properties

| Property | Required | Applies to | Description |
|---|---|---|---|
| `id` | yes | docs, llm-wiki | Stable document ID |
| `title` | yes | docs, llm-wiki | Human readable title |
| `type` | yes | docs, llm-wiki | Controlled document type |
| `category` | yes | docs, llm-wiki | Controlled document category |
| `status` | yes | docs, llm-wiki | Lifecycle state |
| `owner` | yes | docs, llm-wiki | Responsible owner |
| `domain` | yes | docs, llm-wiki | Domain or bounded context |
| `project` | yes | docs, llm-wiki | Project identifier |
| `workspace` | yes | docs, llm-wiki | `docs` or `llm-wiki` |
| `created` | yes | docs, llm-wiki | Creation date |
| `updated` | yes | docs, llm-wiki | Last update date |
| `sensitivity` | yes | docs, llm-wiki | Data sensitivity |
| `truth_level` | yes | docs, llm-wiki | Trust level |
| `source_policy` | yes | docs, llm-wiki | How the source should be used |
| `source_of_truth` | yes | docs, llm-wiki | Upstream grounding sources |
| `related` | yes | docs, llm-wiki | Related documents |
| `verified_against` | conditional | llm-wiki | Evidence used to verify workspace notes |
| `confidence` | conditional | llm-wiki | Confidence for workspace observations |
| `tags` | yes | docs, llm-wiki | Namespaced search and graph tags |

## Docs Template Shape

```yaml
---
id: doc-id
title: Document title
type: technical_note
category: methodology
status: draft
owner: unknown
domain: unknown
project: unknown
workspace: docs
created: YYYY-MM-DD
updated: YYYY-MM-DD
sensitivity: internal
truth_level: official
source_policy: source_of_truth
source_of_truth:
  - docs/grounding/manifest.md
related:
  - docs/grounding/sources.md
upstream: []
downstream: []
tags:
  - kms/metadata
---
```

## llm-wiki Template Shape

```yaml
---
id: wiki-id
title: Workspace note title
type: module_note
category: agent_workspace
status: observed
owner: agent
domain: unknown
project: unknown
workspace: llm-wiki
created: YYYY-MM-DD
updated: YYYY-MM-DD
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: medium
source_of_truth:
  - docs/grounding/manifest.md
verified_against:
  - docs/
  - tests/
  - src/
related:
  - llm-wiki/source-map.md
upstream: []
downstream: []
tags:
  - workspace/module-note
  - agent/context
---
```

## Rules

1. Official docs use `workspace: docs`.
2. Workspace notes use `workspace: llm-wiki`.
3. Workspace notes must not use `truth_level: official`.
4. Workspace notes should use `source_policy: workspace_only` or `reference_only`.
5. Every important workspace claim must reference docs, tests, or src.
6. Obsidian seed notes under `docs/_obsidian/property-values/` exist only to help property suggestions and are ignored by `rg`.

## Related

- [taxonomy.md](./taxonomy.md)
- [doc-template.md](../_templates/doc-template.md)
- [llm-wiki-template.md](../_templates/llm-wiki-template.md)

## Change Notes

- 2026-04-28: Aligned schema with `o_2.kms_intergrate.md` and Obsidian Properties usage.
