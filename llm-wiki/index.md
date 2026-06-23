---
id: workspace-index
title: Agent Workspace Index
type: workspace_index
category: workspace_index
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
  - AGENTS.md
  - docs/grounding/manifest.md
verified_against:
  - docs/00-governance/metadata-schema.md
  - docs/00-governance/taxonomy.md
related:
  - llm-wiki/source-map.md
  - llm-wiki/codebase-map.md
  - llm-wiki/open-questions.md
  - llm-wiki/assumptions.md
  - llm-wiki/contradiction-log.md
  - llm-wiki/lessons-learned.md
upstream:
  - docs/grounding/manifest.md
downstream:
  - llm-wiki/tasks/current/TASK.md
  - llm-wiki/module-notes/README.md
tags:
  - workspace/index
  - agent/context
---

# Workspace Index

## Purpose

Index agent workspace notes for the cleaned starter repository.

## Context

`llm-wiki/` is not official source of truth. Use it for active task memory, observations, open questions, and verification notes. Promote durable knowledge into `docs/` only when it becomes official.

## Active Task

- [Current task](./tasks/current/TASK.md)
- [Task workspace README](./tasks/README.md)

## Controlled Workspace

- [Source Map](./source-map.md) — maps workspace notes to evidence and official sources.
- [Codebase Map](./codebase-map.md) — records observed repository structure without defining intended behavior.
- [Open Questions](./open-questions.md) — tracks unresolved questions requiring evidence or human confirmation.
- [Assumptions](./assumptions.md) — tracks provisional claims that must not drive implementation as facts.
- [Contradiction Log](./contradiction-log.md) — records conflicts without silently choosing a winner.
- [Lessons Learned](./lessons-learned.md) — retains reusable workspace learning pending promotion.
- [Module Notes](./module-notes/README.md) — indexes bounded, evidence-backed notes about individual modules.

## Usage

Start with the [Grounding Manifest](../docs/grounding/manifest.md), then use this index to locate workspace observations. Treat every `llm-wiki/` entry as non-authoritative until confirmed against official `docs/`, executable specifications, tests, or current implementation evidence. Promote durable knowledge into `docs/` only through an explicit review and grounding update.

## Related

- [Grounding manifest](../docs/grounding/manifest.md)
- [Task methodology](../docs/methodology/Task.md)
