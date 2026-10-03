#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

json=false
short_name=""
number=""
description=()
while (($#)); do
    case "$1" in
        --json) json=true ;;
        --short-name) shift; short_name="${1:?missing short name}" ;;
        --number) shift; number="${1:?missing number}" ;;
        --help|-h) echo "Usage: $0 [--json] [--number N] [--short-name NAME] DESCRIPTION"; exit 0 ;;
        *) description+=("$1") ;;
    esac
    shift
done
[[ ${#description[@]} -gt 0 ]] || { echo "Feature description is required" >&2; exit 1; }

root="$(get_repo_root)"
if [[ -z "$short_name" ]]; then
    short_name="$(printf '%s' "${description[*]}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g;s/^-+|-+$//g;s/-+/-/g' | cut -c1-48)"
fi
if [[ -z "$number" ]]; then
    highest="$(find "$root/specs" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' 2>/dev/null | sed -nE 's/^([0-9]+)-.*/\1/p' | sort -n | tail -1)"
    number=$((10#${highest:-000} + 1))
fi
printf -v prefix '%03d' "$number"
feature_label="$prefix-$short_name"
feature_dir="$root/specs/$feature_label"
mkdir -p "$feature_dir"
printf '{"feature_directory":"%s"}\n' "$feature_dir" > "$root/.specify/feature.json"

if $json; then
    printf '{"FEATURE_NAME":"%s","SPEC_FILE":"%s/spec.md","FEATURE_DIR":"%s","FEATURE_NUM":"%s"}\n' \
        "$feature_label" "$feature_dir" "$feature_dir" "$prefix"
else
    printf 'Feature directory: %s\n' "$feature_dir"
fi
