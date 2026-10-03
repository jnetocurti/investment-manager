---
name: speckit-constitution
description: Create or update the project constitution and governance principles.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: constitution

Use this skill when the user asks to create or amend project governance. Read `$ARGUMENTS`, `.specify/memory/constitution.md`, and `.specify/templates/constitution-template.md`.

Update only the constitution. Preserve project-specific principles that are not explicitly superseded, use normative MUST/SHOULD language, maintain semantic versioning and ratification/amendment dates, and add a short Sync Impact Report as an HTML comment. Do not implement features or edit application code. Report changed principles, the version transition, and any deferred intent.
