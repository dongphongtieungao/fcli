# docs/AGENTS.md

# Documentation KMS Contract

This instruction file applies to every file under `docs/`.

It refines the root `AGENTS.md` for documentation and KMS work only. It must not weaken or override repository level rules, security rules, model isolation rules, validation rules, source of truth rules, or user instructions defined outside `docs/`.

## 1. Mission

The `docs/` directory is the official KMS for this repository.

The purpose of this KMS is to act as an evidence system for software delivery. Documentation must help control code generation, code changes, testing, verification, operation, review, onboarding, and technical decision making.

Core principle:

```text
If a document cannot support action, decision, verification, operation, or learning feedback, it must not be promoted into active KMS.
```

## 2. Scope

This file governs documentation work under `docs/`, including:

1. Creating Markdown documents.
2. Updating Markdown documents.
3. Moving documentation.
4. Merging documentation.
5. Deprecating documentation.
6. Archiving documentation.
7. Deleting documentation.
8. Updating metadata.
9. Updating links between documentation artifacts.
10. Promoting working knowledge from `llm-wiki/` into `docs/`.
11. Updating grounding files when documentation becomes official.

This file does not define source code implementation rules. Source code rules remain controlled by the root `AGENTS.md` and any source level instruction file.

## 3. KMS Control Questions

Before creating, editing, moving, deprecating, archiving, merging, or deleting any document under `docs/`, the agent must answer the questions below.

### 3.1 Source Question

Which existing KMS document, user instruction, task evidence, code evidence, test evidence, incident evidence, or methodology artifact justifies this documentation change?

Required answer:

1. Source path.
2. Source type.
3. Source status.
4. Reason why the source applies.
5. Whether the source is official, draft, observed, or workspace only.

If no valid source exists, do not create an official document. Create a gap report or draft only when that is useful and safe.

### 3.2 Code Evidence Question

If this document is intended to control code generation, code changes, testing, verification, operation, or technical decisions, what evidence role does it play?

Classify the document as one or more of:

1. Requirement evidence.
2. Architecture evidence.
3. Domain evidence.
4. Contract evidence.
5. Test evidence.
6. Verification evidence.
7. Runtime operation evidence.
8. Decision evidence.
9. Guardrail evidence.
10. Historical evidence.

If the evidence role is unclear, keep the document as draft and do not use it to justify implementation.

### 3.3 Conflict Question

Does the new or changed document conflict with existing KMS logic?

Check against:

1. `docs/grounding/manifest.md`.
2. `docs/grounding/manifest.yaml`.
3. `docs/grounding/sources.md`.
4. ADR documents.
5. Glossary documents.
6. C4 or architecture documents.
7. BPMN or business flow documents.
8. application, OpenAPI, UI, schema, or report contracts.
9. BDD, Gherkin, QA matrix, review checklist, DoR, and DoD.
10. Existing runbooks.
11. Existing prompt guardrails.
12. Related `llm-wiki/contradiction-log.md` entries when present.

If conflict exists, do not silently rewrite the conflict away. Produce a conflict report and mark the task as `BLOCKED` unless the user explicitly decides which source wins.

### 3.4 Impact Question

What is affected if this document is added, changed, deprecated, archived, merged, or deleted?

Check impact on:

1. Source code.
2. Tests.
3. Runtime commands.
4. Generated artifact behavior.
5. Data access or persistence behavior.
6. Presentation, visual, or user-facing artifact behavior.
7. application interface behavior.
8. Configuration behavior.
9. Runbooks.
10. QA matrix.
11. Prompt guardrails.
12. Grounding manifest.
13. Other documentation links.

Do not delete or deprecate a document until replacement, impact, and traceability are clear.

### 3.5 Evidence Quality Question

Is this document good enough to be used as evidence for code or operations?

A document is valid evidence only when it has:

1. Clear purpose.
2. Clear type.
3. Clear status.
4. Clear scope.
5. Source or rationale.
6. Related artifacts.
7. Verification method or expected usage.
8. Owner, or `unknown` when owner is not known.
9. Review date or updated date.
10. No unresolved contradiction.

If any item is missing, the document may exist as draft, but it must not be treated as authoritative implementation evidence.

### 3.6 Classification Question

Is this document placed in the correct artifact layer?

Classify the document as one of:

1. `vision`.
2. `glossary`.
3. `architecture`.
4. `nfr`.
5. `adr`.
6. `standards`.
7. `bpmn`.
8. `executable_spec`.
9. `contract_spec`.
10. `qa_matrix`.
11. `quality_matrix`.
12. `review_checklist`.
13. `dor`.
14. `dod`.
15. `runbook`.
16. `methodology`.
17. `quickstart`.
18. `prompt`.
19. `guide`.
20. `technical_note`.
21. `decision_log`.
22. `research`.
23. `meeting`.
24. `evidence_index`.
25. `traceability_map`.

If no category fits, do not create a generic note. Propose a category first.

## 4. Documentation Source Precedence

When documents conflict, use this precedence order unless the current user task explicitly overrides it:

1. Current user instruction.
2. Root `AGENTS.md`.
3. This `docs/AGENTS.md`.
4. `docs/grounding/manifest.md`.
5. `docs/grounding/manifest.yaml`.
6. `docs/grounding/sources.md`.
7. Accepted ADR.
8. Contract or executable spec.
9. QA matrix, review checklist, DoR, and DoD.
10. Runbook.
11. Methodology documents.
12. `llm-wiki/` workspace notes.
13. Current code implementation.
14. Agent inference.

Rules:

1. `docs/` defines intended system knowledge.
2. `tests/` defines executable or verifiable behavior.
3. `src/` defines current implementation state.
4. `llm-wiki/` defines observations, assumptions, contradictions, and task memory.
5. `src/` must not override official docs unless the task is explicitly to reconcile drift.
6. `llm-wiki/` must not override official docs.
7. Agent inference is always the weakest source.

## 5. Required Retrieval Workflow

Before editing any document under `docs/`, search before reading whole files.

Preferred commands when `tools/kb.ps1` exists:

```powershell
.\tools\kb.ps1 search "<keyword>"
.\tools\kb.ps1 docs "<keyword>"
.\tools\kb.ps1 grounding
.\tools\kb.ps1 audit-metadata
.\tools\kb.ps1 contradictions
.\tools\kb.ps1 context "<keyword>"
```

If `tools/kb.ps1` is not available, use `rg`:

```powershell
rg -n --heading --smart-case "<keyword>" docs
rg -n --heading --smart-case "type:|status:|source_of_truth:|related:|tags:" docs -g "*.md"
rg -n --heading --smart-case "BLOCKED|TBD|TODO|FIXME|assumption|open question|contradiction" docs llm-wiki tests
```

Search requirements:

1. Search by task keyword.
2. Search by module name.
3. Search by artifact type.
4. Search by status.
5. Search by source of truth marker.
6. Search related `llm-wiki/` notes when the task depends on prior agent findings.
7. Read only the relevant files returned by search.
8. Do not scan all of `docs/` unless the task is a documentation audit.

## 6. Markdown Creation Rules

Every new Markdown file under `docs/` must include:

1. YAML frontmatter.
2. Exactly one H1.
3. Purpose or Summary.
4. Context.
5. Source of truth or References.
6. Related or See also.
7. Verification or Usage.
8. Review notes or Change notes when useful.

Minimum frontmatter:

```yaml
---
type: technical_note
status: draft
owner: unknown
domain: unknown
project: unknown
created: YYYY-MM-DD
updated: YYYY-MM-DD
sensitivity: internal
source_of_truth:
  - path/to/source.md
related:
  - path/to/related.md
tags:
  - kms
---
```

Do not invent `source_of_truth`. Only list files actually read, explicit user instructions, or real repository artifacts.

If a field is unknown, use `unknown`.

## 7. Markdown Editing Rules

When editing an existing Markdown file under `docs/`:

1. Preserve original meaning unless the task explicitly changes it.
2. Preserve existing links unless they are wrong.
3. Preserve code fences.
4. Preserve tables.
5. Preserve existing frontmatter values unless they are wrong or stale.
6. Update `updated` date.
7. Add source or related links only when they are real.
8. Do not create fake backlinks.
9. Do not mix Markdown relative links and Obsidian wikilinks in the same file unless the file already uses both.
10. If normalization would greatly expand scope, report what remains unnormalized and why.

## 8. Lifecycle Status Rules

Use only these statuses:

1. `draft`.
2. `reviewed`.
3. `published`.
4. `deprecated`.

Status meaning:

1. `draft`: useful but not yet approved as official evidence.
2. `reviewed`: checked by human or validated against official source.
3. `published`: active KMS source of truth.
4. `deprecated`: no longer active, kept for traceability.

Rules:

1. Do not mark a document `reviewed` without review evidence.
2. Do not mark a document `published` when source, scope, or contradiction status is unclear.
3. Do not delete a `published` document directly.
4. Deprecate before deleting when the document has inbound references or historical value.
5. Archive or delete only after impact analysis.

## 9. No Orphan Rule

A new or modified document must not be isolated.

Each document must have at least one of:

1. `source_of_truth` in frontmatter.
2. `related` in frontmatter.
3. References section.
4. See also section.
5. Link from a Map of Content.
6. Link to a parent artifact.
7. Link to a child artifact.

If no trustworthy link exists, keep the document as `draft` and record an open question.

## 10. Spec First Link Rules

When the target exists, preserve or add links along the Spec First chain:

1. Vision links to Glossary, C4, and BPMN.
2. Glossary links to Vision, C4, and BPMN.
3. C4 links to Vision, NFR, ADR, Standards, and Contract.
4. NFR links to Vision, C4, QA Matrix, and DoD.
5. ADR links to C4, Standards, Contract, or related Runbook.
6. Standards link to Guardrails, Review Checklist, and Quality Matrix.
7. BPMN links to Vision, Glossary, BDD, and Contract.
8. Contract links to BPMN, BDD, Guardrails, and QA Matrix.
9. Guardrails link to Standards, Contract, QA Matrix, and Review Checklist.
10. QA Matrix links to BDD, Contract, and Quality Matrix.
11. Review Checklist links to Standards, QA Matrix, and DoD.
12. DoR links to artifacts required before implementation.
13. DoD links to QA Matrix, Review Checklist, and Runbook.
14. Runbook links to Contract, QA Matrix, and DoD when relevant.

Do not add fake links. If a target does not exist, report the traceability gap.

## 11. Documentation Promotion Rules

Working knowledge may be promoted from `llm-wiki/` to `docs/` only when it becomes official knowledge.

Allowed promotion cases:

1. Confirmed requirement.
2. Confirmed architecture decision.
3. Confirmed business rule.
4. Confirmed report contract.
5. Confirmed application interface behavior.
6. Confirmed verification rule.
7. Confirmed runbook procedure.
8. Confirmed incident learning.
9. Confirmed coding standard.
10. Confirmed prompt guardrail.

Promotion report must include:

1. Source workspace note.
2. Target document.
3. Reason for promotion.
4. Evidence used.
5. Affected documents.
6. Whether grounding manifest must be updated.

Do not promote assumptions as official knowledge.

## 12. Deletion, Deprecation, and Archival Rules

Before deleting, deprecating, archiving, or merging a document, produce impact analysis.

Impact analysis must include:

1. Current document path.
2. Current status.
3. Inbound references if known.
4. Outbound references.
5. Related code paths if any.
6. Related tests if any.
7. Related runbooks if any.
8. Replacement document if any.
9. Risk if removed.
10. Recommendation.

Allowed recommendations:

1. keep.
2. update.
3. merge.
4. deprecate.
5. archive.
6. delete.

Deletion rule:

Do not delete when deprecation or archive preserves useful traceability with lower risk.

## 13. Conflict Handling

If documentation conflicts with another source, create or update a contradiction record when useful.

Use this format:

```text
Conflict:
Affected files:
Source A:
Source B:
Observed contradiction:
Impact:
Recommended resolution:
Blocked:
```

If the conflict affects code generation, runtime behavior, business rule, data access, generated output, or validation command, mark the task as `BLOCKED`.

## 14. KMS Evidence Contract

A document can be cited as evidence for code only when all are true:

1. It is under `docs/`.
2. It has clear type.
3. It has clear status.
4. It has clear scope.
5. It has source or rationale.
6. It has related artifact links or references.
7. It is not contradicted by a higher priority source.
8. It has enough detail to produce or verify behavior.

If not, the agent must say the document is not strong enough as implementation evidence.

## 15. Traceability Contract

Any official document that controls code, tests, verification, operation, or technical decisions must be traceable.

Minimum traceability:

1. Requirement or task source.
2. Spec or contract source.
3. Decision source when architecture is affected.
4. Code path when implementation is affected.
5. Test or verification path when behavior is affected.
6. Runbook path when operation is affected.
7. Grounding manifest entry when the document becomes canonical.

If traceability is missing, report a traceability gap.

## 16. Drift Control

After a documentation change, classify drift impact as one of:

1. `no_drift_detected`.
2. `docs_ahead_of_code`.
3. `code_ahead_of_docs`.
4. `tests_ahead_of_docs`.
5. `docs_conflict_with_tests`.
6. `docs_conflict_with_src`.
7. `grounding_manifest_stale`.

If drift affects behavior, validation, or generated output, do not claim completion until the drift is documented and a resolution path is proposed.

## 17. Documentation Quality Gate

Before completing a documentation task, verify:

1. Correct path.
2. Correct type.
3. Correct status.
4. Correct source of truth.
5. Related links exist where appropriate.
6. No obvious contradiction.
7. No orphan document.
8. No fake source.
9. No ungrounded claim.
10. Original meaning preserved unless intentionally changed.
11. Manifest update considered.
12. Workspace update considered.
13. Dead links checked when within scope.

## 18. Grounding Manifest Update Rule

Update or recommend updating grounding files when:

1. A new official artifact is created.
2. An official artifact path changes.
3. An artifact status changes.
4. An artifact owner changes.
5. An artifact becomes canonical.
6. An artifact is deprecated.
7. A document becomes a source for code, test, validation, or runbook behavior.

Relevant files:

1. `docs/grounding/manifest.md`.
2. `docs/grounding/manifest.yaml`.
3. `docs/grounding/sources.md`.

If manifest update is needed but outside current scope, report it as a traceability gap.

## 19. Documentation Task Brief

Before editing documentation for a non trivial task, produce this brief:

```text
Task Scope:
Document Type:
Artifact Layer:
Grounding Sources:
Official Docs Used:
Workspace Notes Used:
Current Document Findings:
Assumptions:
Contradictions:
Open Questions:
Plan:
Files In Scope:
Files Out Of Scope:
Manifest Impact:
Verification Method:
```

## 20. Documentation Final Report

After editing documentation, report:

```text
Files Changed:
Document Type:
Status:
Source Of Truth Used:
Related Links Added Or Preserved:
Conflict Check:
Traceability Gaps:
Manifest Updates:
Workspace Updates:
Meaning Changed:
Dead Links Or Unverified Links:
Remaining Risks:
Next Actions:
```

## 21. Hard Stops

Stop and report `BLOCKED` when:

1. The requested documentation change contradicts an accepted ADR.
2. The requested documentation change contradicts a contract spec.
3. The requested documentation change changes business logic without source evidence.
4. The requested documentation change would make code generation rely on an unverified note.
5. The requested deletion removes the only source of truth for a code behavior.
6. The requested document has no clear artifact type and cannot be classified.
7. The requested document would create duplicate canonical sources.
8. The user asks to mark a document as reviewed or published without evidence and the repo requires review evidence.
9. Required source files are missing and the agent would need to invent facts.

## 22. Lightweight KMS Principle

Every document in `docs/` must answer at least one of these:

1. What action does this support?
2. What decision does this justify?
3. What code or test does this explain?
4. What behavior does this verify?
5. What operation does this make safer?
6. What future mistake does this prevent?

If none applies, do not promote the document into active KMS.

## 23. Final Principle

`docs/` is not a storage folder. It is an evidence system that controls implementation, verification, operation, and technical decision making.
