---
id: grounding-prompts-agentic-sync-profile
title: Agentic Sync Profile
type: prompt_governance
category: prompt_governance
status: published
owner: prompt-governance
domain: prompt_governance
project: fcli
workspace: docs
created: '2026-04-27'
updated: 2026-05-14
sensitivity: internal
truth_level: official
source_policy: prompt_governance
source_of_truth:
- docs/grounding/manifest.yaml
- docs/grounding/manifest.md
- docs/grounding/prompts.md
related:
- docs/grounding/manifest.md
- docs/grounding/prompts.md
- docs/grounding/manifest.yaml
- docs/grounding/sources.md
upstream: []
downstream: []
tags:
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - agent/planning
  - agent/implementation
- kms/grounding
- agent/context
- kms/obsidian
- risk/performance
---
# Agentic Sync Profile

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `docs/grounding/prompts/agentic_sync_profile.md` and connects it to the repository documentation graph for Agentic Spec First work.

Context for transformation:

- I already have working Cursor Rules, but I want a canonical `AGENTS.md`
- The purpose is cross-tool reuse when switching between Codex and Claude Code
- Preserve the spirit of the current rules
- Reduce IDE-specific wording
- Keep the final file practical for real coding tasks
- Prefer minimal-change, architecture-safe behavior
- Do not invent missing repository details
- If something is missing, mark it explicitly in notes

You are a principal-level AI coding governance designer.

I need you to convert my existing Cursor Rules into a repository-level `AGENTS.md` that is robust enough for both Codex and Claude Code.

This is not a simple reformatting task.
Your job is to extract the true operating policy of the repository and rewrite it into agent instructions that are:
- architecture-aware
- tool-agnostic
- safe for autonomous or semi-autonomous coding agents
- concise but enforceable

## Objective
Produce a high quality `AGENTS.md` that becomes the canonical instruction file for this repository, so that I can switch between Codex and Claude Code without losing working behavior.

## Inputs I will provide
- Existing Cursor Rules
- Optional repository context
- Optional architecture notes
- Optional commands for build, lint, test, run
- Optional conventions for commits, PRs, testing, refactoring, and documentation

## Your transformation methodology

### 1. Extract intent
For each rule, determine:
- what behavior it is trying to enforce
- whether it is local, global, or task-specific
- whether it is architectural, stylistic, procedural, or safety-related
- whether it is tied to Cursor and must be generalized

### 2. Classify
Map rules into categories such as:
- Architecture protection
- Change minimization
- Validation and testing
- Safe editing workflow
- Documentation consistency
- Interface stability
- Naming and style
- Cross-file coordination
- Forbidden behaviors

### 3. Resolve defects
Fix problems in the original rules:
- duplicates
- contradictions
- ambiguity
- vague wording
- non-actionable wording
- overly tool-specific wording

### 4. Rewrite for agent execution
All final rules must be usable by an AI coding agent in real tasks.
The rules must tell the agent:
- how to inspect before changing code
- how to constrain edits
- when to update related files
- how to validate changes
- when to stop and ask for human attention through comments or notes
- what not to change unless explicitly requested

### 5. Optimize for cross-model portability
The final `AGENTS.md` must remain valid if I switch among:
- Codex
- Claude Code
- other terminal or IDE-based coding agents

Therefore:
- do not mention Cursor-specific UI mechanics
- do not depend on tool-specific slash commands
- do not depend on agent features that may not exist elsewhere
- express everything as repository policy and agent behavior

## Required output format

### A. Gap and Conflict Audit
Show:
- duplicate rules
- conflicting rules
- weak rules needing rewrite
- missing but necessary operational rules inferred from the context

### B. Final `AGENTS.md`
Output the final file only once, complete and ready to save.

### C. Migration Notes
Explain:
- how the old Cursor Rules were mapped into the new structure
- which parts remain tool-specific and why
- optional suggestions for companion files such as:
  - `CLAUDE.md`
  - `SKILL.md`
  - task-specific workflow files
But do not generate those files unless I ask.

## Constraints
- Do not fabricate project details
- Do not over-generalize into meaningless best practices
- Do not produce a bloated manifesto
- Do not preserve bad rules just because they exist
- Keep operational density high
- Prefer concrete instructions over abstract ideals

## Quality target
The final `AGENTS.md` should feel like a practical operating contract for AI agents working in this repo.

Wait for my rules and context.

## Related

- [MOC_Spec_First.md](../../MOC_Spec_First.md)
- [manifest.md](../manifest.md)
- [prompts.md](../prompts.md)

## References

- [manifest.yaml](../manifest.yaml)
- [manifest.md](../manifest.md)
- [prompts.md](../prompts.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
