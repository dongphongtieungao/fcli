# BPMN-to-BDD traceability

## Purpose

This matrix proves that each published main business flow has a happy-path acceptance scenario and identifies the requirement source for the supporting control and exception scenarios.

## Main-flow acceptance coverage

| BPMN / use case | Happy-path scenario | Requirement basis | Coverage status |
|---|---|---|---|
| UC-001 — Start local Bridge | `Start the Bridge with a valid authenticated workspace Agent` | FR001, FR004–FR005 | Covered |
| UC-002 — Authenticate or recover access | `Require browser login when no valid login exists` | FR002 | Covered |
| UC-003 — Synchronize workspace Agent | `Create and cache a missing workspace Agent` | FR005–FR006 | Covered |
| UC-004 — Obtain chat completion | `Return a non-streaming chat completion`; `Relay a streaming chat completion` | FR007–FR010 | Covered |
| UC-005 — Direct HTTP smoke test | `Run the documented direct HTTP smoke test` | FR013 | Covered |
| UC-006 — Controlled OpenCode pilot | `Perform a non-destructive analysis in OpenCode` | FR011–FR012 | Covered |

## Material control and exception coverage

| BPMN condition | Scenario | Requirement basis | Expected acceptance result |
|---|---|---|---|
| Model cannot be resolved | `Stop startup when the required model cannot be resolved` | FR004 | Clear error; no model substitution. |
| Agent sync fails | `Fail safely when Agent synchronization fails` | FR005–FR006 | Explicit failure; no inline-instruction fallback. |
| Unsupported model | `Reject an unsupported model without fallback` | FR004 | `model_not_supported`; no upstream fallback. |
| Unsupported tool requirement | `Reject an unsupported tool requirement without executing it` | FR012 | Safe rejection; Bridge never executes a tool. |
| Transient failure / first chunk boundary | `Retry only before the first response chunk` | FR009 | Retry is limited to before output; no whole-request retry after relay. |
| Client cancellation | `Close the upstream stream when the client cancels` | FR009 | Upstream closed and connection released. |
| OpenCode approval decline | `Respect a declined OpenCode approval` | FR011–FR012 | Declined action is not performed. |

## Explicit gaps that prevent executable automation

The canonical requirement does not define the exact OpenCode request payload, PrivateGPT Agent API schema, ChatRequest schema, SSE event schema, error payloads, timeout values, retry budget, or secure token-store implementation. These specifications intentionally assert observable behavior only. Add step definitions and runtime fixtures only after those questions are confirmed; do not encode assumptions as acceptance facts.

## Sources

- [Canonical requirement](../../docs/01-requirements/00.requirement.md)
- [Use cases](../../docs/01-requirements/01.use-cases.md)
- [BPMN](../../docs/03-business-analysis/09.bpmn.md)
