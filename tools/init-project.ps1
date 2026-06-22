<#
.SYNOPSIS
  Initialize the SDD starter repository for a new project.

.DESCRIPTION
  Replaces the starter placeholder 'fcli' with the given project name
  across all Markdown and YAML files in docs/, llm-wiki/, and root governance files.
  Also cleans llm-wiki/tasks/current/ to empty templates.

.PARAMETER ProjectName
  The new project name to use (e.g. 'my-app', 'order-service').

.PARAMETER DryRun
  If set, only reports what would change without modifying files.

.EXAMPLE
  .\tools\init-project.ps1 -ProjectName my-new-project
  .\tools\init-project.ps1 -ProjectName my-new-project -DryRun
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectName,

    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$OldName = 'fcli'

# Directories to scan
$ScanPaths = @(
    (Join-Path $Root 'docs'),
    (Join-Path $Root 'llm-wiki'),
    (Join-Path $Root 'specs')
)

# Root files to update
$RootFiles = @(
    'AGENTS.md', 'GEMINI.md', 'CLAUDE.md', 'README.md',
    '.cursorrules'
) | ForEach-Object { Join-Path $Root $_ } | Where-Object { Test-Path $_ }

$Extensions = @('*.md', '*.yaml', '*.yml')
$ChangedCount = 0

Write-Host "=== SDD Starter Project Init ===" -ForegroundColor Cyan
Write-Host "Old name : $OldName"
Write-Host "New name : $ProjectName"
Write-Host "Dry run  : $DryRun"
Write-Host ""

# Collect all files
$Files = @()
foreach ($dir in $ScanPaths) {
    if (Test-Path $dir) {
        foreach ($ext in $Extensions) {
            $Files += Get-ChildItem -Path $dir -Filter $ext -Recurse -File
        }
    }
}
foreach ($rf in $RootFiles) {
    $Files += Get-Item $rf
}

# Deduplicate
$Files = $Files | Sort-Object FullName -Unique

foreach ($file in $Files) {
    $content = Get-Content -Path $file.FullName -Raw -ErrorAction SilentlyContinue
    if (-not $content) { continue }

    if ($content -match [regex]::Escape($OldName)) {
        $newContent = $content -replace [regex]::Escape($OldName), $ProjectName
        if ($DryRun) {
            Write-Host "[DRY] Would update: $($file.FullName)" -ForegroundColor Yellow
        } else {
            Set-Content -Path $file.FullName -Value $newContent -NoNewline
            Write-Host "[OK]  Updated: $($file.FullName)" -ForegroundColor Green
        }
        $ChangedCount++
    }
}

Write-Host ""
Write-Host "Files $( if ($DryRun) { 'that would be' } else { '' } ) updated: $ChangedCount" -ForegroundColor Cyan

# Clean llm-wiki/tasks/current/
$CurrentTaskDir = Join-Path (Join-Path (Join-Path $Root 'llm-wiki') 'tasks') 'current'
if (Test-Path $CurrentTaskDir) {
    Write-Host ""
    Write-Host "Cleaning llm-wiki/tasks/current/ to empty templates..." -ForegroundColor Cyan
    $taskFiles = Get-ChildItem -Path $CurrentTaskDir -Filter '*.md' -File
    foreach ($tf in $taskFiles) {
        if (-not $DryRun) {
            $basename = $tf.BaseName
            $header = "---`ntype: technical_note`nstatus: draft`ntags: []`n---`n`n# $basename`n`nEmpty starter template.`n"
            Set-Content -Path $tf.FullName -Value $header -NoNewline
            Write-Host "[OK]  Reset: $($tf.FullName)" -ForegroundColor Green
        } else {
            Write-Host "[DRY] Would reset: $($tf.FullName)" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "=== Done ===" -ForegroundColor Cyan
if (-not $DryRun) {
    Write-Host "Next steps:"
    Write-Host "  1. Review changes with: git diff"
    Write-Host "  2. Run: .\tools\kb.ps1 audit-ci"
    Write-Host "  3. Commit when satisfied"
}
