---
id: workspace-module-notes-index
title: Module Notes Index
type: workspace_index
category: workspace_module
status: synthesized
owner: agent
domain: privategpt-adapter
project: fcli
workspace: llm-wiki
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
truth_level: working
source_policy: workspace_only
confidence: high
source_of_truth:
  - AGENTS.md
  - docs/grounding/manifest.md
verified_against:
  - docs/00-governance/metadata-schema.md
  - docs/00-governance/taxonomy.md
related:
  - llm-wiki/index.md
  - llm-wiki/codebase-map.md
upstream:
  - llm-wiki/codebase-map.md
downstream: []
tags:
  - workspace/module-note
  - agent/context
---

# Module Notes Index

## Purpose

Provide a controlled index for evidence-backed observations about individual modules or bounded implementation areas.

## Context

Module notes describe current observations for agent work. They are not official requirements, architecture decisions, contracts, or sources of truth.

## Usage

Create a focused note only after identifying the module boundary and reading the applicable official artifacts. Use `type: module_note`, `category: workspace_module`, `source_policy: workspace_only`, and a non-official `truth_level`. Include concrete `verified_against` paths for code, tests, specifications, or documentation that were inspected.

Do not copy official documentation into this directory. Link to it, record observations and gaps, and promote durable confirmed knowledge through the controlled `docs/` workflow.

## Module Notes

- [PrivateGPT Adapter](./privategpt-adapter.md) — current Python module boundaries, verification surface and known runtime gaps.

## Related

- [Workspace Index](../index.md)
- [Codebase Map](../codebase-map.md)
- [Grounding Manifest](../../docs/grounding/manifest.md)
