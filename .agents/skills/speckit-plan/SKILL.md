---
name: speckit-plan
description: Produce the technical implementation plan and supporting design artifacts for the active feature.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: plan

Run `.specify/scripts/bash/setup-plan.sh --json` once. Read the active `spec.md`, live constitution, repository guidance, and architecture documentation. Fill `plan.md` with the actual technical context, constitution gates, affected project structure, and implementation approach.

Resolve every `NEEDS CLARIFICATION` relevant to implementation through focused research. Record decisions and rejected alternatives in `research.md`; create `data-model.md`, `contracts/`, and `quickstart.md` only when the feature needs them. Keep domain policy separated from infrastructure and specify compatibility, test strategy, migrations, observability, and—when probabilistic—eval metrics and thresholds. Re-check constitution gates after design.

Do not implement. Report generated artifacts and readiness for `$speckit-tasks`.
