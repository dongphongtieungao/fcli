---
id: workspace-source-map
title: Workspace Source Map
type: source_map
category: workspace_source_map
status: synthesized
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-05-13
updated: 2026-06-23
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: high
source_of_truth:
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
verified_against:
  - docs/grounding/manifest.yaml
  - docs/00-governance/metadata-schema.md
related:
  - llm-wiki/index.md
  - llm-wiki/codebase-map.md
upstream:
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
downstream:
  - llm-wiki/tasks/current/
tags:
  - workspace/source-map
  - kms/grounding
---

# Source Map

## Purpose

Map workspace observations back to official sources.

## Context

Use this when promoting workspace findings into official docs.

The official source registry remains [docs/grounding/manifest.md](../docs/grounding/manifest.md) and [docs/grounding/sources.md](../docs/grounding/sources.md). This table is only a working map; it cannot promote or override a source by itself.

## Map

| Workspace Note | Official Source | Status |
|---|---|---|
| `tasks/current/deletion-audit.md` | Pending promotion decision | workspace only |

## Related

- [Workspace Index](./index.md)
- [Codebase Map](./codebase-map.md)
