$ErrorActionPreference = "Stop"

$bases = @(
  "docs/_obsidian/property-values",
  "docs/00-governance/obsidian-property-values"
)
$today = Get-Date -Format "yyyy-MM-dd"

$sets = @{
  "category" = @(
    "grounding", "governance", "methodology", "requirement", "architecture",
    "business_analysis", "spec_test", "quality_assurance", "ci_cd_review",
    "runbook", "operation", "prompt_governance", "agent_workspace",
    "workspace_index", "workspace_memory", "workspace_source_map",
    "workspace_codebase", "workspace_module", "workspace_question",
    "workspace_assumption", "workspace_contradiction", "workspace_risk",
    "workspace_lesson", "workspace_verification"
  )
  "type" = @(
    "methodology", "guide", "technical_note", "governance", "manifest",
    "source_catalog", "prompt_governance", "vision", "glossary", "requirement",
    "use_case", "architecture_c4", "nfr", "adr", "standards", "bpmn",
    "executable_spec", "openapi", "cli_spec", "ui_spec", "prompt_guardrails",
    "qa_matrix", "quality_matrix", "review_checklist", "dor", "dod", "runbook",
    "workspace_index", "source_map", "project_memory", "codebase_map", "module_note",
    "task_memory", "open_questions", "assumptions", "contradiction_log", "risk_log",
    "lessons_learned", "verification_notes", "agent_runbook"
  )
  "status" = @(
    "draft", "reviewed", "published", "deprecated", "archived",
    "observed", "synthesized", "verified", "promoted", "stale", "discarded"
  )
  "truth_level" = @(
    "official", "working", "observed", "assumption", "archived"
  )
  "source_policy" = @(
    "source_of_truth", "source_catalog", "machine_registry", "methodology",
    "prompt_governance", "workspace_only", "reference_only"
  )
  "tags" = @(
    "kms/grounding", "kms/metadata", "kms/taxonomy", "kms/rg", "kms/obsidian",
    "sdd/vision", "sdd/glossary", "sdd/c4", "sdd/nfr", "sdd/adr",
    "sdd/bpmn", "sdd/bdd", "sdd/openapi", "sdd/cli-spec", "sdd/ui-spec",
    "sdd/guardrails", "sdd/qa", "sdd/dor", "sdd/dod",
    "agent/context", "agent/planning", "agent/implementation",
    "agent/verification", "agent/debugging", "agent/refactoring",
    "workspace/index", "workspace/source-map", "workspace/codebase-map",
    "workspace/module-note", "workspace/open-question", "workspace/assumption",
    "workspace/contradiction", "workspace/lesson", "workspace/verification",
    "risk/security", "risk/performance", "risk/data", "risk/integration",
    "risk/operation", "status/draft", "status/reviewed", "status/published",
    "status/deprecated", "status/observed", "status/verified"
  )
}

foreach ($base in $bases) {
  foreach ($group in $sets.Keys) {
    $dir = Join-Path $base $group
    New-Item -ItemType Directory -Force -Path $dir | Out-Null

    foreach ($value in $sets[$group]) {
      $fileName = ($value -replace '/', '-')
      $file = Join-Path $dir "$fileName.md"
      $frontmatterValue = if ($group -eq "tags") { "tags:`n  - $value`n  - kms/taxonomy" } else { "${group}: $value`ntags:`n  - kms/taxonomy" }
      $title = "${group}: $value"

      $content = @"
---
id: seed-$group-$fileName
title: "$title"
$frontmatterValue
---
# $title

## Purpose

Seed note for Obsidian property value suggestions.

## Related

- [taxonomy.md](../../../00-governance/taxonomy.md)
"@

      Set-Content -Path $file -Value $content -Encoding UTF8
    }
  }
}

Write-Host "Obsidian taxonomy seed notes created under:"
foreach ($base in $bases) {
  Write-Host "- $base"
}
Write-Host "Date: $today"
