---
name: spec-acceptance-mapping
description: Map a behavior, contract, application, runtime, data-backed logic, or user-visible change to requirements, business flow, acceptance criteria, contracts, tests, runbooks, and evidence. Use before implementation or review whenever business behavior, generated output, application interface behavior, or acceptance evidence matters.
---

# Spec Acceptance Mapping

Use this skill before implementation when behavior or contract may change.

Canonical guide: `docs/grounding/skills/4.spec-acceptance-mapping.md`.

Procedure:

1. Read the canonical guide.
2. Identify the affected behavior, actor, input, output, rule, error path, and expected change.
3. Search requirements, glossary, BPMN, feature files, BDD, application/generated-output contracts, QA matrix, and runbook sources.
4. Create observable acceptance criteria; do not invent business rules.
5. Map each AC to source, contract/runbook, test or evidence, and gap.

Output:

```text
Spec Acceptance Map
Affected behavior:
Affected actor or user:
Upstream business sources:
Contract or interface sources:
Existing executable specs:
Runbook or validation sources:
Acceptance criteria:
AC to evidence map:
Mapping status:
Missing tests:
Missing sources:
Open questions:
Implementation gate:
Recommended next skill:
```
