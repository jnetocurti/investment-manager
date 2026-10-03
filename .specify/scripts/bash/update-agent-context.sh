#!/usr/bin/env bash

set -euo pipefail
SCRIPT_DIR="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

root="$(get_repo_root)"; branch="$(get_current_branch)"; require_feature_branch "$branch"
plan="$(feature_dir_from_branch "$root" "$branch")/plan.md"
[[ -f "$plan" ]] || { echo "Arquivo ausente: $plan" >&2; exit 1; }

# AGENTS.md contém intencionalmente orientações duradouras do repositório. Escolhas
# tecnológicas específicas da funcionalidade permanecem no plan.md, sem serem copiadas.
printf 'O contexto de agentes já está definido em %s; use o plano da funcionalidade em %s\n' "$root/AGENTS.md" "$plan"
