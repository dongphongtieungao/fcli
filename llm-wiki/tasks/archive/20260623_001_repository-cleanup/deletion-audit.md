---
type: technical_note
status: draft
owner: unknown
domain: workspace
project: fcli
created: 2026-05-13
updated: 2026-05-13
sensitivity: internal
source_of_truth:
  - AGENTS.md
  - docs/AGENTS.md
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
related:
  - TASK.md
  - analysis.md
  - verification-notes.md
tags:
  - workspace/task
  - cleanup-audit
---

# Deletion Audit

## Purpose

Record the root cause of the over-deletion and classify deleted files by reuse value.

## Context

The cleanup task removed old project-specific material but initially treated many reusable documentation scaffold files as disposable. This audit corrects that by separating domain content from reusable starter structure.

## Root Cause

The cleanup had three connected failure modes:

1. It used legacy keyword presence and old-project path names as the dominant deletion signal. That was too coarse and caused reusable scaffold to be deleted.
2. It replaced some official registry files with minimal placeholders. For files such as `docs/grounding/manifest.md`, `manifest.yaml`, and `sources.md`, "domain-neutral" is not enough; they must remain populated because they route source IDs, coverage, citations, and task behavior.
3. It sanitized documents unevenly. Some docs were cleaned while related methodology, prompt, and skill files still carried old project terms such as report models, old runbook paths, and old release handover paths.

The safer decision model should have been:

1. Delete old domain content.
2. Keep or rewrite reusable scaffold.
3. Preserve empty structural folders with `.gitkeep` when they teach the project layout.
4. Sanitize governance and template documents instead of deleting them.
5. Preserve registry semantics for manifest, sources, prompts, taxonomy, and skill docs.
6. Run a cross-file scan for stale terminology after every cleanup pass.

## Classification Rules

| Classification | Rule |
|---|---|
| Restore generic | File role is reusable across projects but content had old domain assumptions. |
| Keep deleted | File is old domain requirement, business analysis, release artifact, audit, fixture, generated output, or stale workspace note. |
| Keep empty scaffold | Directory is useful for project structure but old contents are not reusable. |
| Already retained | File was not deleted or was rewritten earlier in the task. |

## Deleted File Groups Reviewed

| Group | Decision | Rationale |
|---|---|---|
| `.agent/skills/oracle-rac-refactoring-lane/SKILL.md` | Keep deleted | Repo-specific skill for old domain. |
| `docs/00-governance/00.*.md` | Restore generic | Governance concepts are reusable; old content was project-specific. |
| `docs/00-governance/base/*.md` | Restore generic | Baseline governance structure is reusable. |
| `docs/00-governance/Template/*.md` | Restore generic | Templates are useful starter assets after sanitization. |
| `docs/00-governance/vi/*.md` | Restore generic | Vietnamese governance copies are useful for local onboarding after being rewritten from the sanitized English baseline. |
| `docs/01-requirements/*.md` | Restore generic active index | Core requirement and use-case documents are useful starter artifacts after removing old project assumptions. |
| `docs/01-requirements/Template/*.md` | Restore generic | Requirement and use-case templates are reusable. |
| `docs/01-requirements/tmp/**` | Keep deleted | Temporary old-project analysis and images. |
| `docs/02-architecture/*.md` | Restore generic active index | Vision, glossary, C4, NFR, ADR, structure, and standards docs are reusable Spec First scaffold after sanitization. |
| `docs/02-architecture/Template/*.md` | Restore generic | Architecture templates are reusable. |
| `docs/02-architecture/Template/c4/**` | Restore generic text/PUML templates; keep PNG deleted | PlantUML/text C4 templates are reusable; generated PNG outputs remain old artifacts. |
| `docs/02-architecture/diagrams/**` | Keep empty scaffold | User preference confirmed diagrams folder should remain empty; `.gitkeep` added. |
| `docs/03-business-analysis/*.md` | Restore generic active index | Glossary, BPMN, BA, and UI analytics starter docs are reusable after sanitization. |
| `docs/03-business-analysis/Template/*.md` | Restore generic | Business glossary and BPMN templates are reusable. |
| `docs/03-business-analysis/refactoring/**` | Keep deleted | Old project refactoring records. |
| `docs/03-business-analysis/slide*/**` | Keep deleted | Old report/slide specs, images, CSV examples, and SQL migration docs. |
| `docs/04-implementation/**` | Keep deleted | Old implementation plan and solution docs. |
| `docs/04-spec-test/10.*`, `slide*_testcase.md` | Keep deleted | Old contracts and old report test cases. |
| `docs/04-spec-test/application-spec.md` | Restore generic | Active starter application interface placeholder retained after sanitization. |
| `docs/04-spec-test/11.*`, `12.*` | Restore generic | Schema/API lint governance is reusable after sanitization. |
| `docs/04-spec-test/Template/**` | Restore generic subset | Generic OpenAPI, application interface, JSON Schema, and API lint templates retained. |
| `docs/05-environment-setup/*.md` | Keep deleted | Old environment setup docs were domain-specific. |
| `docs/05-environment-setup/Template/environment-setup.md` | Restore generic | Environment setup template is reusable. |
| `docs/06-quality-assurance/13.*`, `14.*` | Restore generic | Prompt guardrails and QA matrix are reusable after sanitization. |
| `docs/06-quality-assurance/Template/**` | Restore generic | QA templates are reusable. |
| `docs/07-ci-cd-review/15.*`, `16.*` | Restore generic | Quality matrix and review checklist are reusable. |
| `docs/07-ci-cd-review/Template/**` | Restore generic | CI/review templates are reusable. |
| `docs/09-runbook/17.*`, `18.*` | Restore generic | DoR and DoD are reusable. |
| `docs/09-runbook/4.*`, `5.*`, `HYBRID_*`, `test-env-runbook.md` | Keep deleted | Old operational runbooks. |
| `docs/09-runbook/Template/**` | Restore generic | DoR/DoD templates are reusable. |
| `docs/10-release/sdd-case-study-report.md` | Restore generic | Case-study outline is reusable when emptied of old release evidence. |
| Other `docs/10-release/**` | Keep deleted | Old release and handover material. |
| `docs/99-audit/**` | Keep deleted | Old audit findings. |
| `docs/archive/**` | Keep deleted for old artifacts | Archive contents were old-domain records; recreate generic MOCs only when needed. |
| `docs/audit/current-state-audit-report.md` | Restore generic | Audit report template is reusable as a starter current-state review. |
| `docs/context/**` | Keep deleted | Old module context. |
| `docs/grounding/Template/**` | Restore generic | Grounding templates are reusable after removing old-domain examples. |
| `docs/grounding/manifest.md`, `manifest.yaml`, `sources.md` | Restore populated registry | These are not optional placeholders; they must retain source groups, source IDs, coverage, and usage rules. |
| `docs/grounding/prompts/avg/**`, `capture.md`, `renderer.md`, `slides-*.md`, `refactoring.md` | Keep deleted | Old module-specific prompt lanes. |
| `docs/grounding/skills/**`, `.agents/skills/**` | Restore generic | Skill contracts are reusable, but old report-model terminology must be generalized to domain/runtime model terminology. |
| `llm-wiki` core workspace notes | Restore generic | Open questions, assumptions, contradiction log, source map, lessons learned, and verification notes are reusable task memory scaffolds. |
| `llm-wiki/module-notes/**` | Keep deleted | Workspace-only notes from old implementation. |
| `ruff_errors.txt`, `.coverage` | Keep deleted | Generated diagnostics. |
| `specs/10.openapi.yaml`, `specs/Template/10.openapi.yaml`, `specs/schemas/tools/application.schema.json` | Restore generic | Starter API and application interface contract placeholders are reusable after sanitization. |

## Restored Generic Assets

- Governance active docs and templates under `docs/00-governance/`.
- Requirement, architecture, business analysis, spec, environment, QA, review, and runbook templates.
- Empty architecture diagrams scaffold with `.gitkeep`.
- Generic active docs for requirements, architecture, and business analysis.
- Generic C4 text/PlantUML templates, with generated PNGs intentionally left deleted.
- Starter prompt governance in `docs/grounding/prompts.md`.
- Generic QA, quality, review, DoR, and DoD docs.
- Generic grounding templates under `docs/grounding/Template/`.
- Populated grounding registry in `docs/grounding/manifest.md`, `docs/grounding/manifest.yaml`, and `docs/grounding/sources.md`.
- Generic skill terminology for task intake, context retrieval, code boundary analysis, acceptance mapping, and verification.
- Generic Vietnamese governance notes under `docs/00-governance/vi/`.
- Generic release case-study outline under `docs/10-release/`.

## Reuse Upgrade Pass

After the initial restoration, key active docs were upgraded from thin placeholders into reusable frameworks. The target shape is:

1. Purpose and context explain reusable role.
2. Principles, rules, matrix, checklist, or record shape guide future work.
3. Promotion or maintenance rules explain when workspace knowledge moves to official docs.
4. Related links connect to manifest, sources, QA, DoR/DoD, or neighboring artifacts.
5. No legacy domain terminology is required for the framework to work.

Files upgraded with this pattern include requirement, use-case, business requirement, architecture glossary, vision, C4, NFR, ADR, project structure, standards, business analysis, BPMN, contract governance, QA matrix, quality matrix, review checklist, DoR, DoD, audit report, eval notes, release case-study, docs README, and prompt guardrails.

## Related

- [TASK.md](./TASK.md)
- [analysis.md](./analysis.md)
- [verification-notes.md](./verification-notes.md)
