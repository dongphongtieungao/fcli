---
type: runbook
status: draft
owner: unknown
domain: privategpt-adapter
project: fcli
created: 2026-06-23
updated: 2026-06-23
sensitivity: internal
source_of_truth:
  - docs/01-requirements/00.requirement.md
  - docs/04-spec-test/application-spec.md
  - docs/02-architecture/04.architecture-c4.md
  - Current user instruction, 2026-06-23
related:
  - 18.dod.md
  - ../04-spec-test/application-spec.md
  - ../06-quality-assurance/14.qa-matrix.md
  - ../02-architecture/04.architecture-c4.md
tags:
  - runbook
  - testing
  - privategpt-adapter
---

# Test Runbook — OpenCode PrivateGPT Bridge

## Purpose

Provide safe local checks for the Bridge without exposing credentials or claiming a real PrivateGPT/OpenCode pilot.

## Context

The local API, model whitelist, Provider SPI mapping and SSE parser have unit coverage. Browser OAuth, secure refresh-token storage and real upstream/OpenCode wire compatibility require approved credentials and redacted runtime captures.

## Prerequisites

1. Install dependencies: `uv sync`.
2. Edit `config.env` only for non-secret values such as the PrivateGPT base URL and model ID. Do not put tokens or API keys in this file.
3. Set secrets only in the current shell when an approved test account is available:

```powershell
$env:PRIVATEGPT_ACCESS_TOKEN = "<approved-test-token>"
$env:PRIVATEGPT_ADAPTER_API_KEY = "<local-api-key>"
```

## Test Steps

1. Run automated tests:

```powershell
uv run pytest -q
```

2. Validate configuration and the CLI output:

```powershell
uv run python -m src.cli opencode config
```

3. Start the local API from Windows Explorer or a command prompt:

```cmd
run.cmd
```

4. In a second PowerShell window, verify health and the model whitelist:

```powershell
Invoke-RestMethod http://127.0.0.1:4100/health
Invoke-RestMethod http://127.0.0.1:4100/v1/models -Headers @{ Authorization = "Bearer $env:PRIVATEGPT_ADAPTER_API_KEY" }
```

5. Only after PrivateGPT credentials and model/Agent runtime evidence are approved, run the direct non-stream smoke request from requirement FR013. Record a redacted response and never commit tokens or prompts.

## Expected Results

- Tests pass.
- `run.cmd` binds only to `127.0.0.1:4100`.
- `/health` returns `{"status":"ok"}`.
- `/v1/models` lists only `gemini-2.5-pro`.
- Without a valid upstream token, chat returns `login_required`; this is expected and safer than a fallback.

## Limitations

This runbook does not verify browser login, secure token persistence, token refresh/retry, cancellation handling against a live upstream, or an OpenCode pilot. Those gates require the unresolved contracts and approved runtime evidence described in the Application Spec.

## Related

- [Requirement](../01-requirements/00.requirement.md)
- [Application Spec](../04-spec-test/application-spec.md)
- [C4 Architecture](../02-architecture/04.architecture-c4.md)
- [QA Matrix](../06-quality-assurance/14.qa-matrix.md)
- [Definition of Done](./18.dod.md)
