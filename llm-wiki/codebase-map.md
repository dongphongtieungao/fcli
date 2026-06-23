---
id: workspace-codebase-map
title: Workspace Codebase Map
type: codebase_map
category: workspace_codebase
status: observed
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-05-13
updated: 2026-06-23
sensitivity: internal
truth_level: observed
source_policy: workspace_only
confidence: medium
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.md
verified_against:
  - src/
  - tests/
  - specs/
related:
  - llm-wiki/index.md
  - llm-wiki/source-map.md
  - llm-wiki/module-notes/README.md
upstream:
  - AGENTS.md
  - docs/grounding/manifest.md
downstream:
  - llm-wiki/module-notes/
tags:
  - workspace/codebase
  - agent/context
---

# Codebase Map

## Purpose

Map current repository structure for agent work.

## Context

This map describes observed repository structure. It must be rechecked against the filesystem before use and must not override intended behavior in official documentation.

## Map

| Path | Role |
|---|---|
| `src/privategpt_adapter/` | Installable Python Bridge package: API, service, Provider SPI, PrivateGPT adapter, SSE parser, configuration and CLI composition. |
| `tests/unit/` | Executable unit/API/provider contract tests with fake providers and mock HTTP transport. |
| `tests/features/` | Gherkin acceptance specifications; not yet bound to a BDD runner. |
| `docs/` | Official project knowledge |
| `specs/` | Machine-readable contracts |
| `llm-wiki/` | Workspace-only notes |
| `tools/` | Helper scripts |

## Usage

Use this file to find implementation surfaces after grounding in official documentation. Put detailed, module-specific observations under [Module Notes](./module-notes/README.md), with evidence paths and verification status.

## Related

- [Workspace Index](./index.md)
- [Source Map](./source-map.md)
