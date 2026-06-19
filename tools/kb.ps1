param(
    [Parameter(Position=0)]
    [string]$Command = "help",

    [Parameter(Position=1)]
    [string]$Query = ""
)

$Docs = "docs"
$Workspace = "llm-wiki"
$Tests = "tests"
$Src = "src"
$Grounding = "docs/grounding"

function Require-Query {
    if ([string]::IsNullOrWhiteSpace($Query)) {
        Write-Host "Missing query"
        exit 1
    }
}

function Invoke-Rg {
    param([string[]]$RgArgs)

    $RepoRoot = Split-Path -Parent $PSScriptRoot
    $ConfigPath = Join-Path $RepoRoot ".ripgreprc"
    $PreviousConfig = $env:RIPGREP_CONFIG_PATH

    if (Test-Path -LiteralPath $ConfigPath) {
        $env:RIPGREP_CONFIG_PATH = $ConfigPath
    }

    try {
        & rg @RgArgs -g "!docs/_obsidian/property-values/**" -g "!docs/00-governance/obsidian-property-values/**" -g "!docs/_templates/**"
    }
    finally {
        $env:RIPGREP_CONFIG_PATH = $PreviousConfig
    }
}

function Status {
    Write-Host "KMS operating model"
    Invoke-Rg @("-n", "docs/|llm-wiki|manifest.md|manifest.yaml|sources.md|prompts.md|AGENTS.md|PowerShell|rg", "AGENTS.md", "docs/methodology/o_2.kms_intergrate.md", "docs/methodology/o_3.kms_obsidian_rg_runbook.md")
}

function Grounding {
    Write-Host "Grounding files"
    Invoke-Rg @("-n", "Purpose|Grounding|Source|Manifest|precedence|source_id|prompt|lane", $Grounding, "-g", "*.md", "-g", "*.yaml")
}

function DocsOnly {
    Require-Query
    Invoke-Rg @("-n", "-i", $Query, $Docs, "-g", "*.md", "-g", "*.yaml", "-g", "*.yml")
}

function WorkspaceOnly {
    Require-Query
    if (-not (Test-Path $Workspace)) {
        Write-Host "Workspace not found: $Workspace"
        exit 1
    }
    Invoke-Rg @("-n", "-i", $Query, $Workspace, "-g", "*.md")
}

function Specs {
    Require-Query
    Invoke-Rg @("-n", "-i", $Query, "docs/04-spec-test", $Tests)
}

function Code {
    Require-Query
    Invoke-Rg @("-n", "-i", $Query, $Src, $Tests)
}

function Search {
    Require-Query
    Invoke-Rg @("-n", "-i", $Query, $Docs, $Workspace, $Tests, $Src)
}

function Blockers {
    Invoke-Rg @("-n", "-i", "BLOCKED|TBD|TODO|FIXME|unclear|assumption|open question|contradiction|không đủ bằng chứng|insufficient grounding", $Docs, $Workspace, $Tests, "-g", "*.md", "-g", "*.feature", "-g", "*.py")
}

function Audit {
    Write-Host "# KMS audit"
    Write-Host "`n## Grounding summary"
    Grounding
    Write-Host "`n## Metadata markers"
    Audit-Metadata
    Write-Host "`n## Markdown files missing frontmatter"
    Missing-Meta
    Write-Host "`n## Markdown files missing links or source references"
    Missing-Links
    Write-Host "`n## Blocker markers"
    Blockers
    Write-Host "`n## Contradictions"
    Contradictions
}

function Audit-CI {
    if (-not (Test-Path -LiteralPath "tools/kms_ci_audit.py")) {
        Write-Error "Missing tools/kms_ci_audit.py; cannot run KMS CI audit."
        exit 1
    }
    python tools/kms_ci_audit.py
}

function Audit-Metadata {
    Write-Host "Metadata markers"
    Invoke-Rg @("-n", "-i", "type:|category:|status:|truth_level:|source_policy:|workspace:|tags:", $Docs, $Workspace, "-g", "*.md")
}

function Missing-Meta {
    $Paths = @()
    if (Test-Path $Docs) { $Paths += $Docs }
    if (Test-Path $Workspace) { $Paths += $Workspace }

    foreach ($Path in $Paths) {
        Get-ChildItem $Path -Recurse -Filter *.md | Where-Object {
            -not (Select-String -Path $_.FullName -Pattern "^---$" -Quiet)
        } | Select-Object FullName
    }
}

function Missing-Links {
    $Paths = @()
    if (Test-Path $Docs) { $Paths += $Docs }
    if (Test-Path $Workspace) { $Paths += $Workspace }

    foreach ($Path in $Paths) {
        Get-ChildItem $Path -Recurse -Filter *.md | Where-Object {
            -not (Select-String -Path $_.FullName -Pattern "\[[^\]]+\]\([^)]+\)|\[\[.*\]\]|source_of_truth:|related:" -Quiet)
        } | Select-Object FullName
    }
}

function Contradictions {
    if (Test-Path "$Workspace/contradiction-log.md") {
        Get-Content "$Workspace/contradiction-log.md"
    }
    Invoke-Rg @("-n", "-i", "contradiction|conflict|mâu thuẫn|llm_wiki|not create llm|không tạo thêm", $Docs, $Workspace, "AGENTS.md", "-g", "*.md")
}

function Ready {
    Write-Host "Spec Driven Development readiness"
    Invoke-Rg @("-n", "-i", "03.vision|type: vision|# .*Vision", $Docs)
    Invoke-Rg @("-n", "-i", "02.glossary|type: glossary|# .*Glossary", $Docs)
    Invoke-Rg @("-n", "-i", "04.architecture-c4|type: architecture|C4", $Docs)
    Invoke-Rg @("-n", "-i", "09.bpmn|type: bpmn|business flow|BPMN", $Docs)
    Invoke-Rg @("-n", "-i", "Feature:|Scenario:|Given |When |Then ", $Tests)
    Invoke-Rg @("-n", "-i", "cli-spec|openapi|ui-spec|contract", $Docs)
    Invoke-Rg @("-n", "-i", "prompt-guardrails|qa-matrix|quality-matrix|review-checklists|DoR|DoD", $Docs)
}

function Context-Pack {
    Require-Query
    Write-Host "# Context pack for: $Query"
    Write-Host "`n## Grounding"
    Invoke-Rg @("-n", "-i", $Query, $Grounding, "-g", "*.md", "-g", "*.yaml")
    Write-Host "`n## Official docs"
    Invoke-Rg @("-n", "-i", $Query, $Docs, "-g", "*.md")
    Write-Host "`n## Workspace"
    if (Test-Path $Workspace) {
        Invoke-Rg @("-n", "-i", $Query, $Workspace, "-g", "*.md")
    }
    Write-Host "`n## Executable specs"
    Invoke-Rg @("-n", "-i", $Query, $Tests)
    Write-Host "`n## Source"
    Invoke-Rg @("-n", "-i", $Query, $Src)
}

function Trace {
    Require-Query
    Write-Host "# Trace for: $Query"
    Write-Host "`n## Official docs"
    Invoke-Rg @("-n", "-i", $Query, $Docs, "-g", "*.md", "-g", "*.yaml", "-g", "*.yml")
    Write-Host "`n## Executable specs and tests"
    Invoke-Rg @("-n", "-i", $Query, $Tests)
    Write-Host "`n## Workspace observations"
    if (Test-Path $Workspace) {
        Invoke-Rg @("-n", "-i", $Query, $Workspace, "-g", "*.md")
    } else {
        Write-Host "Workspace not found: $Workspace"
    }
    Write-Host "`n## Source implementation"
    Invoke-Rg @("-n", "-i", $Query, $Src)
    Write-Host "`n## Known blockers and contradictions"
    Invoke-Rg @("-n", "-i", "$Query|BLOCKED|TBD|TODO|FIXME|unclear|assumption|open question|contradiction", $Docs, $Workspace, $Tests, "-g", "*.md", "-g", "*.feature", "-g", "*.py")
}

function Sync-Check {
    Require-Query
    Write-Host "# Sync check for: $Query"
    Write-Host "`n## Requirement, business, spec, QA, and runbook evidence"
    Invoke-Rg @("-n", "-i", $Query, "docs/01-requirements", "docs/02-architecture", "docs/03-business-analysis", "docs/04-spec-test", "docs/06-quality-assurance", "docs/07-ci-cd-review", "docs/09-runbook", "-g", "*.md", "-g", "*.yaml", "-g", "*.yml")
    Write-Host "`n## Executable evidence"
    Invoke-Rg @("-n", "-i", $Query, $Tests)
    Write-Host "`n## Workspace context"
    if (Test-Path $Workspace) {
        Invoke-Rg @("-n", "-i", $Query, $Workspace, "-g", "*.md")
    } else {
        Write-Host "Workspace not found: $Workspace"
    }
    Write-Host "`n## Source implementation"
    Invoke-Rg @("-n", "-i", $Query, $Src)
    Write-Host "`n## Open questions and contradictions"
    Invoke-Rg @("-n", "-i", "$Query|open question|assumption|contradiction|BLOCKED|insufficient grounding|không đủ bằng chứng", $Workspace, $Docs, "-g", "*.md")
    Write-Host "`n## Status guidance"
    Write-Host "SYNCED: docs/spec evidence, test evidence, source implementation, and no blocking open question or contradiction are all present."
    Write-Host "PARTIALLY_SYNCED: some evidence exists, but at least one docs/spec/test/source link is missing or weak."
    Write-Host "NOT_SYNCED: source behavior lacks official docs/spec/test grounding, or official docs lack implementation/test evidence."
}

function Help {
    Write-Host "Usage:"
    Write-Host ".\tools\kb.ps1 status"
    Write-Host ".\tools\kb.ps1 grounding"
    Write-Host ".\tools\kb.ps1 docs `"keyword`""
    Write-Host ".\tools\kb.ps1 workspace `"keyword`""
    Write-Host ".\tools\kb.ps1 specs `"keyword`""
    Write-Host ".\tools\kb.ps1 code `"keyword`""
    Write-Host ".\tools\kb.ps1 search `"keyword`""
    Write-Host ".\tools\kb.ps1 blockers"
    Write-Host ".\tools\kb.ps1 audit"
    Write-Host ".\tools\kb.ps1 audit-ci"
    Write-Host ".\tools\kb.ps1 audit-metadata"
    Write-Host ".\tools\kb.ps1 missing-meta"
    Write-Host ".\tools\kb.ps1 missing-links"
    Write-Host ".\tools\kb.ps1 contradictions"
    Write-Host ".\tools\kb.ps1 ready"
    Write-Host ".\tools\kb.ps1 context `"keyword`""
    Write-Host ".\tools\kb.ps1 trace `"feature or module`""
    Write-Host ".\tools\kb.ps1 sync-check `"feature or module`""
}

switch ($Command) {
    "status" { Status }
    "grounding" { Grounding }
    "docs" { DocsOnly }
    "workspace" { WorkspaceOnly }
    "specs" { Specs }
    "code" { Code }
    "search" { Search }
    "blockers" { Blockers }
    "audit" { Audit }
    "audit-ci" { Audit-CI }
    "audit-metadata" { Audit-Metadata }
    "missing-meta" { Missing-Meta }
    "missing-links" { Missing-Links }
    "contradictions" { Contradictions }
    "ready" { Ready }
    "context" { Context-Pack }
    "trace" { Trace }
    "sync-check" { Sync-Check }
    default { Help }
}
