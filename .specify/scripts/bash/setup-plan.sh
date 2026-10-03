#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

json=false; [[ "${1:-}" == "--json" ]] && json=true
root="$(get_repo_root)"; feature_dir="$(get_feature_dir "$root")"
[[ -f "$feature_dir/spec.md" ]] || { echo "Missing $feature_dir/spec.md" >&2; exit 1; }
"$SCRIPT_DIR/resolve-template.sh" plan-template "$feature_dir/plan.md"
if $json; then
    printf '{"FEATURE_SPEC":"%s/spec.md","IMPL_PLAN":"%s/plan.md","FEATURE_DIR":"%s"}\n' "$feature_dir" "$feature_dir" "$feature_dir"
else
    printf '%s\n' "$feature_dir/plan.md"
fi
