# AGENTS.md - Starter Repository Operating Contract

## 0. Precedence

When instructions or evidence conflict, apply this order:

1. Current user task, unless it requests unsafe, destructive, illegal, or explicitly forbidden behavior.
2. This root `AGENTS.md`.
3. More local `AGENTS.md` files for the touched path, such as `docs/AGENTS.md`.
4. Official artifacts under `docs/`.
5. Executable specs and tests under `tests/`.
6. Workspace notes under `llm-wiki/`.
7. Current implementation under `src/`.
8. Agent inference.

`docs/` defines intended project knowledge. `llm-wiki/` is workspace memory only and must not override official docs or current user instructions.

## 1. Baseline Guardrails

- Do not hardcode secrets, credentials, connection strings, tokens, or PII.
- Do not introduce destructive filesystem, database, or network behavior without explicit task scope.
- Keep runtime output structured and avoid logging sensitive data.
- Prefer small, reversible changes with clear verification evidence.
- Preserve user changes. Do not revert unrelated work.
- Keep implementation details in code and durable project decisions in `docs/`.

## 2. Repository Shape

This repository is currently a starter structure. The expected top-level roles are:

| Path | Role |
|---|---|
| `src/` | Application source code for the new project |
| `tests/` | Automated tests and executable examples |
| `docs/` | Official project knowledge base and Spec-First artifacts |
| `docs/grounding/` | Source registry, prompt routing, and grounding guidance |
| `docs/methodology/` | Delivery methodology and agent workflow |
| `llm-wiki/` | Agent workspace notes and active task memory |
| `specs/` | Machine-readable contracts and schemas |
| `tools/` | Local helper scripts |

Do not recreate legacy project-specific domain models, data layouts, or validation lanes unless the user explicitly defines them for the new project.

## 3. Before Editing

For tiny edits, read this file, any local `AGENTS.md`, and the touched file.

For code changes, also identify:

- entry point and caller/callee boundary;
- config or contract impact;
- smallest useful verification command;
- whether docs or tests need to change with the code.

For documentation changes under `docs/`, read `docs/AGENTS.md` and apply its KMS rules.

## 4. Documentation Rules

Official documentation must be useful for action, decision, verification, operation, onboarding, or future maintenance.

When creating or editing Markdown under `docs/`, prefer:

- YAML frontmatter with `type`, `status`, `owner`, `domain`, `project`, `created`, `updated`, `sensitivity`, `source_of_truth`, `related`, and `tags`;
- exactly one H1;
- `Purpose` or `Summary`;
- `Context` when the document is operational or architectural;
- real related links and source references.

Do not invent source-of-truth links. If evidence is missing, mark the document `draft` and record open questions.

### 4.1 Obsidian Wiki Documentation Gate

This gate applies before an agent creates or modifies any `.md` file in the repository. Before editing, determine and verify:

1. The document type and its artifact layer in the Agentic Spec First model.
2. The governing source of truth and whether it was actually read.
3. The upstream and downstream artifacts to which the document should link.
4. The lifecycle status: `draft`, `reviewed`, `published`, or `deprecated`.
5. Whether the document participates in the Vision, Glossary, C4, NFR, ADR, Standards, BPMN, BDD, Contract, QA, DoR, or DoD chain.
6. The canonical path in `docs/grounding/manifest.yaml`, `docs/grounding/manifest.md`, and the current repository structure.

An agent must not create or edit an isolated note, remove existing traceability, add facts without evidence, or claim `reviewed` or `published` without review or publication evidence. If the canonical artifact already exists, update it rather than creating a parallel path.

### 4.2 Normalize Touched Markdown Files

Every `.md` file created or modified by an agent must be checked and minimally normalized in the same diff without changing its original meaning. For Markdown under `docs/` or `docs/methodology/`, require:

1. Valid YAML frontmatter.
2. Exactly one H1.
3. A `Purpose` or `Summary` section.
4. A `Context` section for methodology, architecture, requirement, QA, runbook, or design artifacts.
5. A `Related` or `See also` section.
6. `source_of_truth` metadata or a `References` section when content derives from another artifact.
7. A valid lifecycle `status`.
8. Real internal links to relevant artifacts.
9. Preservation of code fences, Markdown tables, existing valid links, and original meaning.
10. Compliance with the no-orphan rule in section 4.6.

If normalization would expand the task materially, preserve the file's meaning and structure, make the safe minimum change, and report every remaining gap in the final response. Simple README files, changelogs, licenses, and third-party or generated Markdown are exempt from the full schema, but their Markdown structure must not be damaged.

### 4.3 Minimum Frontmatter

Use the repository metadata schema and taxonomy. A new documentation artifact starts as `draft` unless stronger status evidence exists:

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
  - normalized
---
```

Rules:

1. For files under `docs/`, `type` must use a controlled value from `docs/00-governance/taxonomy.md`. Relevant Spec First types include `technical_note`, `vision`, `glossary`, `requirement`, `use_case`, `architecture_c4`, `nfr`, `adr`, `standards`, `bpmn`, `executable_spec`, `contract_spec`, `openapi`, `application_spec`, `ui_spec`, `prompt_guardrails`, `qa_matrix`, `quality_matrix`, `review_checklist`, `dor`, `dod`, `runbook`, `prompt`, `guide`, and `methodology`. Do not invent a type; propose a taxonomy change when no controlled value fits.
2. `status` must be one of `draft`, `reviewed`, `published`, or `deprecated`.
3. Preserve an existing `created` date. Set `updated` to the current ISO date (`YYYY-MM-DD`) when the document changes.
4. Use `unknown` when `owner`, `domain`, or `project` cannot be established from evidence.
5. Default `sensitivity` to `internal` unless a governing source states otherwise.
6. Never invent `source_of_truth`; list only an explicit user instruction or a real artifact that was read and supports the content.

### 4.4 Agentic Spec First Artifact Mapping

Resolve paths through the grounding manifest and current repository before editing. The current expected mapping is:

| Artifact | Expected canonical path |
|---|---|
| Vision | `docs/02-architecture/03.vision.md` |
| Glossary | `docs/02-architecture/02.glossary.md` |
| Architecture C4 | `docs/02-architecture/04.architecture-c4.md` |
| NFR | `docs/02-architecture/05.nfr.md` |
| ADR | `docs/02-architecture/06.adr.md` or a manifest-approved file under `docs/02-architecture/adr/` |
| Standards | `docs/02-architecture/08.standards.md` |
| BPMN | `docs/03-business-analysis/09.bpmn.md` |
| Executable specification | `tests/features/*.feature` and `tests/bdd/` |
| Web API contract | `specs/10.openapi.yaml` unless the manifest designates another path |
| Application specification | `docs/04-spec-test/application-spec.md` or a manifest-approved YAML equivalent |
| UI specification | `docs/04-spec-test/ui-spec.md` when present and canonical |
| Prompt guardrails | `docs/06-quality-assurance/13.prompt-guardrails.md` |
| QA matrix | `docs/06-quality-assurance/14.qa-matrix.md` |
| Quality matrix | `docs/07-ci-cd-review/15.quality-matrix.md` |
| Review checklist | `docs/07-ci-cd-review/16.review-checklists.md` |
| DoR | `docs/09-runbook/17.dor.md` |
| DoD | `docs/09-runbook/18.dod.md` |
| Runbook | `docs/09-runbook/` |
| Methodology | `docs/methodology/methodology.md` |
| Document strategy | `docs/methodology/d_3.document_strategy_and_creation_order.md` |
| Quickstart | `docs/methodology/d_1.quickstarts.md` |

If the manifest or repository uses a different path, use the verified canonical path. Do not create a second artifact merely to match this table.

### 4.5 Spec First Link Spine

When the target exists, preserve or add links in `Related`, `See also`, `References`, or an appropriate traceability section:

1. Vision links to Glossary, C4, and BPMN.
2. Glossary links to Vision, C4, and BPMN.
3. C4 links to Vision, NFR, ADR, Standards, and Contract.
4. NFR links to Vision, C4, QA Matrix, and DoD.
5. ADR links to C4, Standards, Contract, or the relevant Runbook.
6. Standards link to Guardrails, Review Checklist, and Quality Matrix.
7. BPMN links to Vision, Glossary, executable Feature or BDD artifacts, and Contract.
8. Feature or BDD Markdown indexes link to BPMN, Contract, and QA Matrix.
9. Contract or Spec links to BPMN, Feature or BDD, Guardrails, and QA Matrix.
10. Guardrails link to Standards, Contract, QA Matrix, and Review Checklist.
11. QA Matrix links to Feature or BDD, Contract, and Quality Matrix.
12. Review Checklist links to Standards, QA Matrix, and DoD.
13. DoR links to artifacts required before implementation.
14. DoD links to QA Matrix, Review Checklist, and Runbook.
15. Runbook links to Contract, QA Matrix, and DoD when relevant.

Do not create fake links or placeholder targets. Non-Markdown executable specs and contracts must be linked through their Markdown companion or traceability index. If a target is absent or cannot be verified, keep the document truthful and report the missing artifact as a traceability gap.

### 4.6 No Orphan Documentation And MOC Rule

A new or modified Markdown document must have at least one trustworthy connection through `source_of_truth`, `related`, `References`, `See also`, a parent or child artifact, or an inbound or outbound Map of Content (MOC) or index link. If no trustworthy connection can be established, retain `status: draft` and record an open question.

For an important artifact, check for an applicable MOC or index. Update it when it exists and is within task scope. If none exists, do not create multiple MOCs during a small task; report the gap and, when useful, recommend one of `MOC_Methodology.md`, `MOC_Architecture.md`, `MOC_ADR.md`, `MOC_Runbooks.md`, `MOC_QA.md`, or `MOC_Spec_First.md` for a separately scoped change.

### 4.7 Link Style

Preserve each file's established link convention. Continue using Markdown relative links where the repository uses them; use Obsidian wikilinks only where that convention is already consistent. Do not mix styles in one file unless it already intentionally does so. Prefer relative Markdown links for content consumed by GitHub, MkDocs, Docusaurus, or GitBook. When moving or renaming a file, update verified inbound and outbound links that fall within task scope.

### 4.8 Markdown Documentation Change Report

When a task creates or modifies Markdown, the final report must state:

1. Every `.md` file created or modified, with its type and status.
2. Sources of truth used.
3. Related links added or preserved.
4. Any traceability gap or missing artifact.
5. Any touched file that could not be fully normalized and why.
6. Dead or unverified links.
7. Whether the original meaning changed.
8. Whether a MOC, grounding manifest, or sources catalog update was performed, unnecessary, or remains required.

## 5. Testing And Verification

Use the smallest verification that matches the change:

- Markdown-only cleanup: file inventory and targeted `rg` checks.
- Code change: targeted tests or executable smoke checks.
- Contract or runtime change: contract validation plus runtime smoke checks where available.
- Deletion cleanup: pre/post inventory and search evidence.

Final reports for code or documentation changes should include what changed, what was checked, and any residual risk.

## 6. Skills And Task Workspace

Use `llm-wiki/tasks/current/` for active task memory when a task needs traceability.

Task notes are not official source of truth. Promote durable knowledge into `docs/` only when the task explicitly changes official project knowledge and the evidence is clear.

## 7. Current Starter Status

This repo has been cleaned toward a new-project starter. Some methodology and KMS assets remain intentionally generic. If the new project needs domain-specific rules, add them deliberately through official docs and update grounding sources at the same time.
