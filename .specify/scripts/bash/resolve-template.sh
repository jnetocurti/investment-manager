#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

[[ $# -ge 1 ]] || { echo "Usage: $0 TEMPLATE_NAME [DESTINATION]" >&2; exit 1; }
root="$(get_repo_root)"; name="${1%.md}"; source="$root/.specify/templates/$name.md"
[[ -f "$source" ]] || { echo "Template not found: $name" >&2; exit 1; }
if [[ $# -ge 2 ]]; then cp "$source" "$2"; else printf '%s\n' "$source"; fi
