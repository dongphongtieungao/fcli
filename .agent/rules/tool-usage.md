# Tool Usage Rule

Follow `AGENTS.md`, especially section 13: Agent Tool Usage Overlay.

For SDD/KMS retrieval, verification, and sync workflow, also follow `.agent/rules/sdd-antigravity.md`.

Before reading files:
- Run `git status --short` and inspect relevant `git diff`.
- Use targeted `rg` for symbols, text, tests, configs, entry points, and references.
- Use `fd` for file names, extensions, and path patterns.

When reading:
- Read only relevant line ranges.
- Prefer `bat --paging=never --line-range A:B <file>`.
- If `bat` is unavailable, use `rg -n -C` or `pwsh` with `Get-Content | Select-Object -Skip N -First M`.
- Do not dump long terminal output.

When validating:
- Run the smallest targeted test first.
- Expand validation only after targeted checks pass.
- If `uv run` is unavailable, use the repo's documented Python command without installing tools.

When reporting:
- Summarize command, result, key evidence, and next action.
- Do not paste long raw logs.
