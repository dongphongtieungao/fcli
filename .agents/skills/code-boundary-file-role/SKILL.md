---
name: code-boundary-file-role
description: Analyze code boundaries and file roles before changing important code. Use for entry points, pipelines, adapters, renderers, factories, validation harnesses, shared modules, data-backed logic, or any task where callers, callees, tests, local AGENTS policy, and module isolation must be understood.
---

# Code Boundary File Role

Use this skill before touching important source files.

Canonical guide: `docs/grounding/skills/5.code-boundary-file-role.md`.

Procedure:

1. Read the canonical guide.
2. Identify target files and module boundary.
3. Identify impacted module or bounded context: module name, `shared`, or `n/a`.
4. For model-specific code, read local `AGENTS.md`, relevant interfaces, factories, registries, adapters, schemas, and data-access contracts.
5. Find direct callers, callees, runtime entry points, config/wiring references, tests, and validation surface.
6. Do not cross model directories unless the task is explicitly shared.

Output:

```text
Code Boundary Result
Target files:
Module:
Impacted domain/runtime model:
Boundary summary:
Local policy read:
Official sources read:
Workspace notes read:
Callers:
Callees:
Related tests:
Contracts affected:
Risk:
File Role Snapshot:
Header update needed:
Module note update needed:
Implementation readiness:
Recommended next skill:
```
