#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
json=false
[[ "${1:-}" == "--json" ]] && json=true
root="$(get_repo_root)"; branch="$(get_current_branch)"; require_feature_branch "$branch"
feature_dir="$(feature_dir_from_branch "$root" "$branch")"
[[ -f "$feature_dir/spec.md" ]] || { echo "Arquivo ausente: $feature_dir/spec.md" >&2; exit 1; }
cp "$root/.specify/templates/plan-template.md" "$feature_dir/plan.md"
if $json; then printf '{"FEATURE_SPEC":"%s/spec.md","IMPL_PLAN":"%s/plan.md","SPECS_DIR":"%s","BRANCH":"%s"}\n' "$feature_dir" "$feature_dir" "$root/specs" "$branch"; else printf '%s\n' "$feature_dir/plan.md"; fi
