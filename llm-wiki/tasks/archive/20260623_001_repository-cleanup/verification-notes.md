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
  - docs/grounding/skills/9.create-current-task.md
  - docs/methodology/Task.md
related:
  - TASK.md
  - analysis.md
tags:
  - workspace/task
  - verification
---

# Verification Notes

## Purpose

Define expected verification for the cleanup task without claiming the cleanup has passed.

## Context

No repository cleanup verification has run yet. Task creation only prepared workspace notes.

## Expected Checks

| Check | Command or method | Status |
|---|---|---|
| Current task files created | Inspect `llm-wiki/tasks/current/` | Done |
| Legacy reference inventory | Targeted `rg` scan for old domain terms, old mode names, old artifact names, and obsolete validation tooling | Done |
| File inventory | `rg --files` | Done |
| Post-clean scan | Repeat targeted legacy scans | Done |
| Markdown traceability | Review edited `.md` frontmatter, related links, and source references | Done for created/replaced starter docs |
| Deleted file audit | Review deleted paths and classify reuse value | Done in `deletion-audit.md` |

## Evidence

Post-clean scan excluding `llm-wiki/tasks/current/`:

```powershell
rg -n --heading --smart-case "<legacy-domain-keyword-regex>" -g "!*.png" -g "!*.jpg" -g "!*.jpeg" -g "!*.zip" -g "!.git/**" -g "!llm-wiki/tasks/current/**"
```

Result: no matches for legacy project terms in the active repo content scanned. A separate scan of hidden agent files found only a generic cloud-architecture phrase in `.agent/skills/architect-review/SKILL.md`, not the old report mode.

Post-restore docs and starter-spec scan with word boundaries:

```powershell
rg -n --heading --smart-case "<legacy-domain-keyword-regex>" docs specs AGENTS.md README.md .cursorrules -g "*.md" -g "*.yaml" -g "*.json" -g "*.yml"
```

Result: no matches in active docs, starter specs, root instructions, or README.

Remaining deleted-path review after reusable restoration:

```powershell
git status --short | Where-Object { $_ -like ' D *' } | ForEach-Object { $_.Substring(3) } | Sort-Object
```

Result: remaining deleted files are limited to old `docs/01-requirements/tmp/**`, generated PNG diagrams under architecture templates, and old `docs/archive/**` notes.

Post-root-cause correction scan for stale old-project terminology:

```powershell
rg -n --heading --smart-case "<legacy-domain-and-old-runbook-regex>" AGENTS.md README.md .cursorrules docs specs .agents\skills -g "*.md" -g "*.yaml" -g "*.yml" -g "*.json" -g "SKILL.md"
```

Result: no matches for old report-model, old release handover, old runbook path, or old domain markers in the scanned active docs/specs/skills.

Registry and contract parse checks:

```powershell
python - <<'PY'
import yaml, json
...
PY
```

Result: `docs/grounding/manifest.yaml`, `docs/grounding/Template/manifest.yaml`, OpenAPI starter files, and `specs/schemas/tools/application.schema.json` parsed successfully.

Reusable framework review:

```powershell
python <changed-docs-audit>
```

Result: active docs were reviewed for thin placeholder patterns and upgraded where they were meant to act as reusable framework documents rather than blank templates.

Case-insensitive legacy project metadata check:

```powershell
rg -n -i "oracle-rac-performance-analytics" docs .agents AGENTS.md README.md .cursorrules
```

Result: old lowercase project identifier was replaced with the starter project identifier in docs and skill metadata.

Application terminology cleanup:

```powershell
rg -n "<legacy-application-interface-patterns>" AGENTS.md README.md .cursorrules .agents docs specs llm-wiki -g "*.md" -g "*.yaml" -g "*.yml" -g "*.json" -g "SKILL.md"
```

Result: no stale application-interface artifacts were found. Remaining matches were framework names or UI click examples, not legacy application-interface contract artifacts.

Reusable application spec verification:

```powershell
python <json-yaml-parse-check>
Test-Path docs/04-spec-test/application-spec.md
Test-Path docs/04-spec-test/Template/application-spec.md
Test-Path specs/schemas/tools/application.schema.json
```

Result: JSON/YAML parsed successfully for grounding manifest and starter specs; active application spec, template, and schema files exist.

Reusable SDD framework review against methodology and lifecycle:

```powershell
rg -n "(?i)\bcli\b|cli[-_]|prompt-cli|cli\.schema|cli-spec|10\.cli" docs .agents specs llm-wiki AGENTS.md README.md -g "*.md" -g "*.yaml" -g "*.json" -g "SKILL.md"
rg -n "<known-corruption-markers>" docs/grounding docs/methodology AGENTS.md README.md -g "*.md" -g "*.yaml" -g "*.json"
python <json-yaml-parse-check>
python <grounding-sources-path-check>
.\tools\kb.ps1 audit-ci
```

Result:

- No stale CLI contract/prompt/schema references were found in the scanned SDD/KMS surfaces.
- No known bulk-replacement corruption markers were found in `docs/grounding`, `docs/methodology`, `AGENTS.md`, or `README.md`.
- JSON/YAML parse checks passed for `docs/grounding/manifest.yaml`, template manifest, OpenAPI starter files, and `specs/schemas/tools/application.schema.json`.
- `docs/grounding/sources.md` catalog paths resolve.
- After `tools/kms_ci_audit.py` was restored, `.\tools\kb.ps1 audit-ci` and `python tools/kms_ci_audit.py` both passed the blocking KMS audit gate. The gate reports 35 missing doc links as warnings, not blocking failures.

Reusable metadata normalization review:

```powershell
git diff --check
python <yaml-json-parse-check>
python <changed-docs-frontmatter-field-check>
rg -n --heading --smart-case "<stale-cli-or-script-corruption-markers>" docs/00-governance/taxonomy.md docs/grounding docs/04-spec-test specs
```

Result:

- Changed Markdown files under `docs/` have `type`, `category`, `status`, and `tags` frontmatter when frontmatter is present.
- `docs/grounding/manifest.yaml`, OpenAPI starter files, and `specs/schemas/tools/application.schema.json` parse successfully.
- No whitespace errors were reported by `git diff --check`.
- No stale CLI taxonomy/prompt/schema markers were found in the active grounding/spec surfaces scanned.
- Remaining `status: draft` entries are templates, placeholders, examples, or embedded examples rather than active reusable framework docs.

Antigravity SDD adapter implementation verification:

```powershell
python <manifest-yaml-parse-and-source-path-check>
python <antigravity-skill-inventory-check>
.\tools\kb.ps1 docs "antigravity"
.\tools\kb.ps1 audit-metadata
.\tools\kb.ps1 audit-ci
git diff --check -- GEMINI.md .agent\rules\sdd-antigravity.md .agent\rules\security-and-autonomy.md .agent\rules\tool-usage.md .agent\skills docs\antigravity\playbook.md docs\grounding\manifest.yaml docs\grounding\sources.md docs\grounding\prompts.md
```

Result:

- `docs/grounding/manifest.yaml` parsed and all 58 source paths resolved.
- 9 Antigravity SDD adapter skills exist under `.agent/skills/` and each references its canonical `docs/grounding/skills/` guide.
- `.\tools\kb.ps1 docs "antigravity"` returns the bridge, playbook, source catalog, manifest, and prompt governance entries.
- `.\tools\kb.ps1 audit-ci` passed the blocking KMS audit gate; it still reports 35 missing doc links as warnings, not blocking failures.
- `git diff --check` reported no whitespace errors for the Antigravity adapter files and touched grounding docs.

## Verdict

File cleanup and starter documentation rewrite: verified by targeted inventory scans.

## Related

- [TASK.md](./TASK.md)
- [analysis.md](./analysis.md)
