#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"
require_tasks=false; include_tasks=false
for arg in "$@"; do [[ "$arg" == "--require-tasks" ]] && require_tasks=true; [[ "$arg" == "--include-tasks" ]] && include_tasks=true; done
root="$(get_repo_root)"; branch="$(get_current_branch)"; require_feature_branch "$branch"
feature_dir="$(feature_dir_from_branch "$root" "$branch")"
[[ -f "$feature_dir/spec.md" ]] || { echo "spec.md ausente; execute specify primeiro" >&2; exit 1; }
[[ -f "$feature_dir/plan.md" ]] || { echo "plan.md ausente; execute plan primeiro" >&2; exit 1; }
$require_tasks && [[ ! -f "$feature_dir/tasks.md" ]] && { echo "tasks.md ausente; execute tasks primeiro" >&2; exit 1; }
docs=(); for name in research.md data-model.md quickstart.md contracts; do [[ -e "$feature_dir/$name" ]] && docs+=("$name"); done
$include_tasks && [[ -f "$feature_dir/tasks.md" ]] && docs+=("tasks.md")
joined="$(IFS='","'; echo "${docs[*]-}")"
printf '{"FEATURE_DIR":"%s","AVAILABLE_DOCS":["%s"]}\n' "$feature_dir" "$joined"
