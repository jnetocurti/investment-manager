---
name: speckit-tasks
description: Generate an actionable, dependency-ordered task list for the active feature.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: tasks

Run `.specify/scripts/bash/setup-tasks.sh --json`. Read `spec.md`, `plan.md`, and all available design artifacts plus the live constitution. Generate `tasks.md` from `.specify/templates/tasks-template.md`.

Every item must use `- [ ] T### [P?] [US#?] Description with exact file path`. Organize work into setup, blocking foundations, one phase per prioritized user story, and polish/cross-cutting work. Each story phase needs its goal and independent test. Mark `[P]` only for work on different files with no unmet dependency. Add explicitly requested or constitution-required tests/evals, dependencies, parallel examples, and an MVP-first delivery strategy. Ensure every requirement and entity has an implementation path.

Do not implement tasks. Report counts by story and readiness for `$speckit-analyze` or `$speckit-implement`.
