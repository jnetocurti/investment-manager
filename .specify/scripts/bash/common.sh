#!/usr/bin/env bash

set -e

get_repo_root() {
    if git rev-parse --show-toplevel >/dev/null 2>&1; then
        git rev-parse --show-toplevel
    else
        local script_dir
        script_dir="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
        (cd "$script_dir/../../.." && pwd)
    fi
}

get_feature_dir() {
    local root="$1"
    if [[ -n "${SPECIFY_FEATURE_DIRECTORY:-}" ]]; then
        printf '%s\n' "$SPECIFY_FEATURE_DIRECTORY"
        return
    fi
    if [[ -f "$root/.specify/feature.json" ]]; then
        python3 -c 'import json,sys; print(json.load(open(sys.argv[1], encoding="utf-8"))["feature_directory"])' \
            "$root/.specify/feature.json"
        return
    fi
    echo "No active feature. Run \$speckit-specify first." >&2
    return 1
}

json_array() {
    python3 -c 'import json,sys; print(json.dumps(sys.argv[1:]))' "$@"
}
