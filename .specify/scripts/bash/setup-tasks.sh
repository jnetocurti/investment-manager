#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

root="$(get_repo_root)"; feature_dir="$(get_feature_dir "$root")"
[[ -f "$feature_dir/spec.md" ]] || { echo "Missing $feature_dir/spec.md" >&2; exit 1; }
[[ -f "$feature_dir/plan.md" ]] || { echo "Missing $feature_dir/plan.md" >&2; exit 1; }
template="$root/.specify/templates/tasks-template.md"
docs=(); for name in spec.md plan.md research.md data-model.md quickstart.md contracts; do [[ -e "$feature_dir/$name" ]] && docs+=("$name"); done
printf '{"FEATURE_DIR":"%s","TASKS_TEMPLATE":"%s","AVAILABLE_DOCS":%s}\n' \
    "$feature_dir" "$template" "$(json_array "${docs[@]}")"
