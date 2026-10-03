---
name: speckit-implement
description: Implement the active feature by executing its tasks in dependency order.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: implement

Run `.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks`. Read the live constitution, `tasks.md`, `plan.md`, `spec.md`, and available design artifacts.

Check every checklist under the feature directory before writing code; if any checklist is incomplete, show the count and obtain explicit user confirmation before proceeding. Verify ignore files appropriate to detected tooling. Execute tasks phase by phase in dependency order, honoring `[P]` only when safe. After each completed task, mark its checkbox `[x]`. Preserve repository conventions and run the validation required by each story and by the constitution. Stop on failures that block safe progress; never mark failed or unperformed work complete.

Report completed tasks, validation results, and remaining work. Recommend `$speckit-converge` after implementation.
