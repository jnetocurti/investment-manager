---
name: speckit-checklist
description: Generate a requirements-quality checklist for a requested domain or concern.
compatibility: Requires a Spec Kit project initialized for Codex.
metadata:
  author: github-spec-kit
  source: https://github.com/github/spec-kit
---

# Spec Kit: checklist

Load the active feature artifacts and constitution. Interpret `$ARGUMENTS` as the checklist focus and create a focused file under the feature's `checklists/` directory from `.specify/templates/checklist-template.md`. Items must test whether requirements are complete, clear, consistent, measurable, and scenario-covered—not whether implementation is complete. Use sequential `CHK###` IDs and references to relevant spec sections. Do not mark items complete.
