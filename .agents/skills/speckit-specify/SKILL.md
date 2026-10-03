---
name: speckit-specify
description: Create a feature specification from a natural-language feature description.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: specify

Use this skill to define **what** and **why**, not implementation details. Read `$ARGUMENTS`, the live constitution, and `.specify/templates/spec-template.md`.

1. Require a non-empty feature description.
2. Derive a concise kebab-case short name, allocate the next feature directory under `specs/`, and persist it to `.specify/feature.json` as `feature_directory`. Branch creation is optional and belongs to the official `git` extension, not this core skill.
3. Resolve `spec-template` through the Spec Kit template resolution stack, copy it to the active directory as `spec.md`, and fill it with the feature requirements.
4. Write prioritized, independently testable user stories, Given/When/Then acceptance scenarios, edge cases, numbered functional requirements, key entities when relevant, assumptions, and measurable technology-agnostic success criteria.
5. Create or update `checklists/requirements.md` as a requirements-quality check. Resolve at most three material ambiguities with `[NEEDS CLARIFICATION]`; otherwise document reasonable assumptions.

Do not create a plan, tasks, or implementation. Report the feature directory, checklist result, and readiness for `$speckit-clarify` or `$speckit-plan`.
