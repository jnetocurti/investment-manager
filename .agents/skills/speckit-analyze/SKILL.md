---
name: speckit-analyze
description: Perform read-only consistency and coverage analysis across feature artifacts.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: analyze

Run `.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks`. Read the active `spec.md`, `plan.md`, `tasks.md`, and live constitution. Without modifying files, report duplicates, ambiguities, underspecified requirements, constitution conflicts, missing task coverage, inconsistencies, and orphan tasks. Grade severity, cite artifact locations, provide requirement-to-task coverage metrics, and recommend the smallest corrections before `$speckit-implement`.
