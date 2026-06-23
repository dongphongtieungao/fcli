# BDD acceptance specifications

## Purpose

The Gherkin specifications in [`../features`](../features) translate the published BPMN and use cases into observable acceptance criteria. They are the shared reference for AI-assisted implementation, functional testing, process testing, and business confirmation.

## Scope and source discipline

Each scenario is traceable to the canonical requirement and must not introduce behavior outside that source. The current suite covers the happy path for each published main use case (UC-001 through UC-006), plus the material error and control flows explicitly described in the requirement.

## Structure

| Path | Role |
|---|---|
| [`../features/bridge_lifecycle.feature`](../features/bridge_lifecycle.feature) | Startup, authentication requirement, and model readiness. |
| [`../features/agent_sync.feature`](../features/agent_sync.feature) | Workspace Agent creation, reuse, and safe sync failure. |
| [`../features/chat_completion.feature`](../features/chat_completion.feature) | Chat mapping, streaming, model/tool controls, retry boundary, and cancellation. |
| [`../features/opencode_pilot.feature`](../features/opencode_pilot.feature) | Direct smoke test and controlled OpenCode pilot. |
| [`traceability.md`](./traceability.md) | Mapping from BPMN/use case to requirement and scenario. |

## Usage rules

1. Preserve the `UC-*` and `FR*` tags when changing a scenario.
2. Implement step definitions only against confirmed runtime/API contracts; schemas, error payloads, timeout values, and retry budgets that are still open must not be guessed.
3. A new main flow cannot advance without at least one happy-path scenario containing explicit `Given`, `When`, and `Then` steps.
4. A material exception/control flow described in the requirement must have a scenario before release acceptance.
5. Scenarios test Bridge boundaries. File edits, shell commands, and approvals remain OpenCode responsibilities.

## Current automation status

The repository does not yet define a BDD runner, step-definition framework, or executable API contract. These feature files are acceptance specifications ready for automation after the unresolved PrivateGPT/OpenCode runtime schemas are confirmed. Until then, validate them through review and traceability checks, not by inventing implementation-specific steps.

## Sources

- [Canonical requirement](../../docs/01-requirements/00.requirement.md)
- [Use cases](../../docs/01-requirements/01.use-cases.md)
- [BPMN](../../docs/03-business-analysis/09.bpmn.md)

