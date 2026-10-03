---
name: speckit-converge
description: Assess implementation against spec, plan, tasks, and constitution and append remaining work.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: converge

Run `.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks`. Read `spec.md`, `plan.md`, `tasks.md`, and the live constitution as the sources of intent, then inspect the current implementation within their stated scope.

Verify requirements, success criteria, acceptance scenarios, plan decisions, every task regardless of checkbox state, and constitution MUST principles. Classify gaps as `missing`, `partial`, `contradicts`, or `unrequested` and grade severity. Present findings before writing.

If gaps exist, append exactly one new `## Phase N: Convergence` section to `tasks.md`, using new sequential task IDs and source references; never rewrite prior content or application code. If no gaps exist, leave `tasks.md` byte-for-byte unchanged and report `Converged`. Recommend another `$speckit-implement` pass only when tasks were appended.
