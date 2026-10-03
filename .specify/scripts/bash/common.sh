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

get_current_branch() {
    if [[ -n "${SPECIFY_FEATURE:-}" ]]; then
        printf '%s\n' "$SPECIFY_FEATURE"
    else
        git branch --show-current 2>/dev/null || true
    fi
}

feature_dir_from_branch() {
    local root="$1" branch="$2"
    printf '%s/specs/%s\n' "$root" "$branch"
}

require_feature_branch() {
    local branch="$1"
    [[ "$branch" =~ ^[0-9]{3}- ]] || {
        printf 'Esperada uma branch de funcionalidade no formato ###-nome; recebida: %s\n' "${branch:-<nenhuma>}" >&2
        return 1
    }
}
