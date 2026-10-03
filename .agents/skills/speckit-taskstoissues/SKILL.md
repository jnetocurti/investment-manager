---
name: speckit-taskstoissues
description: Convert the active feature task list into dependency-ordered GitHub issues.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: taskstoissues

Load the active `tasks.md` and repository remote. Confirm the repository is hosted on GitHub and that the available GitHub tooling is authenticated. Convert tasks into dependency-aware issues without changing task intent, preserving task IDs, story labels, file paths, and prerequisites. Do not create duplicate issues; report created issue URLs and skipped items. This command is optional and requires an authenticated GitHub integration.
