---
type: prompt_governance
category: governance
status: draft
owner: unknown
domain: unknown
project: unknown
created: YYYY-MM-DD
updated: YYYY-MM-DD
sensitivity: internal
source_of_truth:
  - docs/grounding/manifest.md
  - docs/grounding/sources.md
  - docs/00-governance/00.data-governance.md
related:
  - 00.data-governance.md
tags:
  - kms/governance
  - prompt/governance
  - sdd/reusable
  - sdd/generic
  - sdd/template
  - risk/data
---

# Prompt Create Data Governance

## Purpose

Guide an agent to draft a data governance document from confirmed requirements.

## Context

Use only after real data sources, owners, sensitivity, and retention expectations are known or explicitly marked unknown.

## Prompt

```text
Create a draft data governance document using the confirmed requirement sources.
Do not invent data sources, owners, retention, or compliance obligations.
Mark unknowns explicitly and link every claim to a source.
```

## Related

- [Data Governance Template](./00.data-governance.md)
