# Security And Autonomy Rule

Follow `AGENTS.md` for repository architecture, grounding, and model-boundary rules.

For SDD/KMS work, also follow `.agent/rules/sdd-antigravity.md`.

Agents may use the editor, terminal, and browser, but actions must stay scoped.

Do not:
- Run destructive commands unless explicitly requested.
- Expose secrets, credentials, connection strings, or PII.
- Modify unrelated files.
- Install tools or dependencies unless requested.
- Create wrapper scripts for tool usage.
- Paste long logs or full file dumps into artifacts or conversation.

Prefer:
- Minimal diffs.
- Targeted search before file reads.
- Relevant diffs before rereading changed files.
- Small validation before full validation.
- Short artifacts with evidence.

If a command fails because a tool is missing from PATH, use the next best listed tool and report the PATH issue briefly.
