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
        --short-name) shift; short_name="${1:?nome curto ausente}" ;;
        --number) shift; number="${1:?número ausente}" ;;
        --help|-h) echo "Uso: $0 [--json] [--number N] [--short-name NOME] DESCRIÇÃO"; exit 0 ;;
        *) description+=("$1") ;;
    esac
    shift
done
[[ ${#description[@]} -gt 0 ]] || { echo "A descrição da funcionalidade é obrigatória" >&2; exit 1; }

root="$(get_repo_root)"
if [[ -z "$short_name" ]]; then
    short_name="$(printf '%s' "${description[*]}" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g;s/^-|-$//g' | cut -c1-48)"
fi
if [[ -z "$number" ]]; then
    highest="$(find "$root/specs" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' 2>/dev/null | sed -nE 's/^([0-9]{3})-.*/\1/p' | sort -n | tail -1)"
    number=$((10#${highest:-000} + 1))
fi
printf -v prefix '%03d' "$number"
branch="$prefix-$short_name"
feature_dir="$root/specs/$branch"
mkdir -p "$feature_dir"
sed "s/\[FEATURE NAME\]/${description[*]}/g; s/\[###-feature-name\]/$branch/g; s/\[DATE\]/$(date +%F)/g; s/\$ARGUMENTS/${description[*]}/g" \
    "$root/.specify/templates/spec-template.md" > "$feature_dir/spec.md"
git switch -c "$branch" >/dev/null 2>&1 || true
printf '{"feature":"%s","directory":"%s"}\n' "$branch" "$feature_dir" > "$root/.specify/feature.json"
if $json; then printf '{"BRANCH_NAME":"%s","SPEC_FILE":"%s/spec.md","FEATURE_NUM":"%s"}\n' "$branch" "$feature_dir" "$prefix"; else printf '%s\n' "$feature_dir/spec.md"; fi
