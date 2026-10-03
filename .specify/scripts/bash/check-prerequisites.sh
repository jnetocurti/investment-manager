#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

json=false; require_tasks=false; include_tasks=false; paths_only=false
for arg in "$@"; do
    [[ "$arg" == "--json" ]] && json=true
    [[ "$arg" == "--require-tasks" ]] && require_tasks=true
    [[ "$arg" == "--include-tasks" ]] && include_tasks=true
    [[ "$arg" == "--paths-only" ]] && paths_only=true
done

root="$(get_repo_root)"; feature_dir="$(get_feature_dir "$root")"
spec="$feature_dir/spec.md"; plan="$feature_dir/plan.md"; tasks="$feature_dir/tasks.md"
if $paths_only; then
    printf 'REPO_ROOT=%s\nFEATURE_DIR=%s\nFEATURE_SPEC=%s\nIMPL_PLAN=%s\nTASKS=%s\n' "$root" "$feature_dir" "$spec" "$plan" "$tasks"
    exit 0
fi
[[ -f "$spec" ]] || { echo "Missing spec.md; run \$speckit-specify first." >&2; exit 1; }
[[ -f "$plan" ]] || { echo "Missing plan.md; run \$speckit-plan first." >&2; exit 1; }
$require_tasks && [[ ! -f "$tasks" ]] && { echo "Missing tasks.md; run \$speckit-tasks first." >&2; exit 1; }
docs=(); for name in research.md data-model.md quickstart.md contracts; do [[ -e "$feature_dir/$name" ]] && docs+=("$name"); done
$include_tasks && [[ -f "$tasks" ]] && docs+=("tasks.md")
printf '{"FEATURE_DIR":"%s","AVAILABLE_DOCS":%s}\n' "$feature_dir" "$(json_array "${docs[@]}")"
