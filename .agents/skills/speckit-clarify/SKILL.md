---
name: speckit-clarify
description: Identify and resolve high-impact ambiguities in the active feature specification.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: clarify

Run `.specify/scripts/bash/check-prerequisites.sh --json --paths-only`, load the active `spec.md` and constitution, and inspect coverage across scope, actors, data, UX, non-functional requirements, failures, constraints, terminology, and completion signals.

Ask one focused question at a time (no more than five), prioritizing answers that materially affect architecture, data, acceptance tests, security, or operations. Offer 2–5 concrete options plus a recommendation when possible. After each answer, immediately update the specification in a `## Clarifications` section dated today and reconcile the affected requirements or scenarios without contradicting earlier text. Validate that no obsolete alternative remains.

Report questions answered, sections changed, remaining low-impact ambiguities, and whether the feature is ready for `$speckit-plan`.
