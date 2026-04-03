#!/bin/bash
#
# Carga completa (flexível): notas de negociação + eventos corporativos
#
# Uso:
#   ./scripts/load-data.sh [opções]
#
# Exemplos:
#   # Todos os processamentos, com parâmetros nomeados (sem depender de ordem)
#   ./scripts/load-data.sh \
#     --notes-dir ~/notas_negociacao \
#     --subscriptions-file scripts/subscricoes-exemplo.json \
#     --splits-file scripts/splits-exemplo.json \
#     --ticker-renames-file scripts/ticker-renames-exemplo.json \
#     --asset-conversions-file scripts/asset-conversions-exemplo.json
#
#   # Apenas eventos corporativos (sem upload de notas)
#   ./scripts/load-data.sh \
#     --processes subscriptions,splits,asset-conversions \
#     --subscriptions-file scripts/subscricoes-exemplo.json \
#     --splits-file scripts/splits-exemplo.json \
#     --asset-conversions-file scripts/asset-conversions-exemplo.json
#
#   # Compatibilidade com chamada legada (ordem fixa)
#   ./scripts/load-data.sh <diretório-notas> [arquivo-subscricoes.json] [arquivo-splits.json] [arquivo-ticker-renames.json]
#
# O arquivo de subscrições é um JSON array:
# [
#   {
#     "subscriptionTicker": "KNCR12",
#     "targetTicker": "KNCR11",
#     "targetAssetType": "REAL_ESTATE_FUND_BRL",
#     "quantity": 36,
#     "unitPrice": 103.54,
#     "totalValue": 3727.44,
#     "fee": 0,
#     "brokerName": "NUINVEST",
#     "brokerDocument": "62.169.875/0001-79",
#     "subscriptionDate": "2024-06-15",
#     "conversionDate": "2024-07-15"
#   }
# ]

set -euo pipefail

API_BASE="http://localhost:8080"
MONGO_CONTAINER="investmentmanager-mongodb"
RABBIT_CONTAINER="investmentmanager-rabbitmq"
DB_NAME="investmentmanager"

# --- Validação e parsing de argumentos ---

NOTES_DIR=""
SUBSCRIPTIONS_FILE=""
SPLITS_FILE=""
TICKER_RENAMES_FILE=""
ASSET_CONVERSIONS_FILE=""
PROCESSES="all"

print_usage() {
  cat <<EOF
Uso:
  $0 [opções]

Opções:
  --notes-dir <dir>                 Diretório com PDFs de notas de negociação
  --subscriptions-file <json>       JSON array para /api/subscriptions
  --splits-file <json>              JSON array para /api/splits
  --ticker-renames-file <json>      JSON array para /api/ticker-renames
  --asset-conversions-file <json>   JSON array para /api/asset-conversions
  --processes <lista>               Lista separada por vírgula:
                                    trading-notes,subscriptions,splits,ticker-renames,asset-conversions,all
  -h, --help                        Exibe esta ajuda

Compatibilidade legada:
  $0 <diretório-notas> [arquivo-subscricoes.json] [arquivo-splits.json] [arquivo-ticker-renames.json]
EOF
}

is_named_mode=false
if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then
  print_usage
  exit 0
fi

for arg in "$@"; do
  case "$arg" in
    --notes-dir|--subscriptions-file|--splits-file|--ticker-renames-file|--asset-conversions-file|--processes)
      is_named_mode=true
      break
      ;;
  esac
done

if [ "$is_named_mode" = true ]; then
  while [ $# -gt 0 ]; do
    case "$1" in
      --notes-dir)
        NOTES_DIR="${2:-}"
        shift 2
        ;;
      --subscriptions-file)
        SUBSCRIPTIONS_FILE="${2:-}"
        shift 2
        ;;
      --splits-file)
        SPLITS_FILE="${2:-}"
        shift 2
        ;;
      --ticker-renames-file)
        TICKER_RENAMES_FILE="${2:-}"
        shift 2
        ;;
      --asset-conversions-file)
        ASSET_CONVERSIONS_FILE="${2:-}"
        shift 2
        ;;
      --processes)
        PROCESSES="${2:-}"
        shift 2
        ;;
      -h|--help)
        print_usage
        exit 0
        ;;
      *)
        echo "Erro: opção desconhecida '$1'"
        print_usage
        exit 1
        ;;
    esac
  done
else
  NOTES_DIR="${1:-}"
  SUBSCRIPTIONS_FILE="${2:-}"
  SPLITS_FILE="${3:-}"
  TICKER_RENAMES_FILE="${4:-}"
fi

should_process() {
  local process_name="$1"
  if [ "$PROCESSES" = "all" ] || [ -z "$PROCESSES" ]; then
    return 0
  fi
  local normalized=",$PROCESSES,"
  [[ "$normalized" == *",$process_name,"* ]]
}

if should_process "trading-notes"; then
  if [ -z "$NOTES_DIR" ]; then
    echo "Erro: --notes-dir é obrigatório quando 'trading-notes' está habilitado."
    print_usage
    exit 1
  fi
  if [ ! -d "$NOTES_DIR" ]; then
    echo "Erro: diretório não encontrado: $NOTES_DIR"
    exit 1
  fi
fi

if should_process "subscriptions" && [ -n "$SUBSCRIPTIONS_FILE" ] && [ ! -f "$SUBSCRIPTIONS_FILE" ]; then
  echo "Erro: arquivo de subscrições não encontrado: $SUBSCRIPTIONS_FILE"
  exit 1
fi

if should_process "splits" && [ -n "$SPLITS_FILE" ] && [ ! -f "$SPLITS_FILE" ]; then
  echo "Erro: arquivo de splits não encontrado: $SPLITS_FILE"
  exit 1
fi

if should_process "ticker-renames" && [ -n "$TICKER_RENAMES_FILE" ] && [ ! -f "$TICKER_RENAMES_FILE" ]; then
  echo "Erro: arquivo de trocas de ticker não encontrado: $TICKER_RENAMES_FILE"
  exit 1
fi

if should_process "asset-conversions" && [ -n "$ASSET_CONVERSIONS_FILE" ] && [ ! -f "$ASSET_CONVERSIONS_FILE" ]; then
  echo "Erro: arquivo de conversões de ativo não encontrado: $ASSET_CONVERSIONS_FILE"
  exit 1
fi

# --- Funções auxiliares ---

wait_for_service() {
  local url=$1
  local max_attempts=30
  local attempt=0
  while [ $attempt -lt $max_attempts ]; do
    if curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null | grep -q "^[2-4]"; then
      return 0
    fi
    attempt=$((attempt + 1))
    sleep 2
  done
  return 1
}

# --- 1. Containers Docker ---

echo "=== Iniciando containers Docker ==="
docker compose up -d 2>&1 | grep -v "^$"
sleep 3

echo "Aguardando MongoDB..."
wait_for_service "" || true
docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "db.runCommand({ping:1})" > /dev/null 2>&1
echo "MongoDB pronto."

echo "Aguardando RabbitMQ..."
attempt=0
while [ $attempt -lt 15 ]; do
  if docker exec "$RABBIT_CONTAINER" rabbitmqctl status > /dev/null 2>&1; then
    break
  fi
  attempt=$((attempt + 1))
  sleep 2
done
echo "RabbitMQ pronto."

# --- 2. Limpeza ---

echo ""
echo "=== Limpando dados ==="

docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "
  db = db.getSiblingDB('$DB_NAME');
  db.trading_notes.drop();
  db.portfolio_events.drop();
  db.position_impact_events.drop();
  db.asset_positions.drop();
  db.asset_position_history.drop();
  print('Collections removidas.');
"

# Limpar filas (ignorar erros se não existirem)
for queue in tradingnote.created.queue portfolioevent.processed.queue portfolioevent.impact.queue assetposition.calculated.dlq portfolioevent.created.queue; do
  docker exec "$RABBIT_CONTAINER" rabbitmqctl purge_queue "$queue" 2>/dev/null || true
done
echo "Filas limpas."

# --- 3. Aguardar aplicação ---

echo ""
echo "=== Verificando aplicação ==="
if wait_for_service "$API_BASE/api/trading-notes/upload"; then
  echo "Aplicação disponível em $API_BASE"
else
  echo "Erro: aplicação não está respondendo em $API_BASE"
  echo "Inicie a aplicação e tente novamente."
  exit 1
fi

# --- 4. Upload de notas ---

if should_process "trading-notes"; then
  echo ""
  echo "=== Enviando notas de negociação ==="

  PDF_COUNT=$(find "$NOTES_DIR" -name "*.pdf" | wc -l | tr -d ' ')
  echo "Encontrados $PDF_COUNT PDFs em $NOTES_DIR"

  SUCCESS=0
  FAIL=0
  SKIPPED=0

  for pdf in $(find "$NOTES_DIR" -name "*.pdf" | sort); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/trading-notes/upload" -F "file=@$pdf" 2>/dev/null)
    if [ "$STATUS" = "200" ]; then
      SUCCESS=$((SUCCESS + 1))
    elif [ "$STATUS" = "422" ]; then
      SKIPPED=$((SKIPPED + 1))
      echo "SKIPPED - $pdf"
    else
      FAIL=$((FAIL + 1))
      echo "  FAIL ($STATUS): $(basename "$pdf")"
    fi
  done

  echo "Notas: $SUCCESS ok, $SKIPPED ignorados (PDF inválido), $FAIL erros"
fi

# --- 5. Subscrições ---

if should_process "subscriptions" && [ -n "$SUBSCRIPTIONS_FILE" ]; then
  echo ""
  echo "=== Enviando subscrições ==="

  SUB_COUNT=$(python3 -c "import json; print(len(json.load(open('$SUBSCRIPTIONS_FILE'))))" 2>/dev/null || echo "0")
  echo "Encontradas $SUB_COUNT subscrições em $SUBSCRIPTIONS_FILE"

  SUB_OK=0
  SUB_FAIL=0

  # Iterar sobre cada objeto do array JSON
  python3 -c "
import json, sys
with open('$SUBSCRIPTIONS_FILE') as f:
    subs = json.load(f)
for s in subs:
    print(json.dumps(s))
" | while IFS= read -r sub_json; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/subscriptions" \
      -H "Content-Type: application/json" \
      -d "$sub_json" 2>/dev/null)
    if [ "$STATUS" = "200" ]; then
      SUB_OK=$((SUB_OK + 1))
    else
      SUB_FAIL=$((SUB_FAIL + 1))
      TICKER=$(echo "$sub_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('subscriptionTicker','?'))" 2>/dev/null)
      echo "  FAIL ($STATUS): $TICKER"
    fi
  done

  echo "Subscrições processadas."
fi

# --- 6. Splits ---

if should_process "splits" && [ -n "$SPLITS_FILE" ]; then
  echo ""
  echo "=== Enviando splits ==="

  SPLIT_COUNT=$(python3 -c "import json; print(len(json.load(open('$SPLITS_FILE'))))" 2>/dev/null || echo "0")
  echo "Encontrados $SPLIT_COUNT splits em $SPLITS_FILE"

  python3 -c "
import json
with open('$SPLITS_FILE') as f:
    for s in json.load(f):
        print(json.dumps(s))
" | while IFS= read -r split_json; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/splits" \
      -H "Content-Type: application/json" \
      -d "$split_json" 2>/dev/null)
    if [ "$STATUS" != "200" ]; then
      TICKER=$(echo "$split_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('targetTicker','?'))" 2>/dev/null)
      echo "  FAIL ($STATUS): $TICKER"
    fi
  done

  echo "Splits processados."
fi

# --- 7. Trocas de ticker ---

if should_process "ticker-renames" && [ -n "$TICKER_RENAMES_FILE" ]; then
  echo ""
  echo "=== Enviando trocas de ticker ==="

  RENAME_COUNT=$(python3 -c "import json; print(len(json.load(open('$TICKER_RENAMES_FILE'))))" 2>/dev/null || echo "0")
  echo "Encontradas $RENAME_COUNT trocas de ticker em $TICKER_RENAMES_FILE"

  python3 -c "
import json
with open('$TICKER_RENAMES_FILE') as f:
    for r in json.load(f):
        print(json.dumps(r))
" | while IFS= read -r rename_json; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/ticker-renames" \
      -H "Content-Type: application/json" \
      -d "$rename_json" 2>/dev/null)
    if [ "$STATUS" != "200" ]; then
      OLD_TICKER=$(echo "$rename_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('oldTicker','?'))" 2>/dev/null)
      NEW_TICKER=$(echo "$rename_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('newTicker','?'))" 2>/dev/null)
      echo "  FAIL ($STATUS): $OLD_TICKER -> $NEW_TICKER"
    fi
  done

  echo "Trocas de ticker processadas."
fi

# --- 8. Conversões/incorporações de ativo ---

if should_process "asset-conversions" && [ -n "$ASSET_CONVERSIONS_FILE" ]; then
  echo ""
  echo "=== Enviando conversões/incorporações de ativo ==="

  CONV_COUNT=$(python3 -c "import json; print(len(json.load(open('$ASSET_CONVERSIONS_FILE'))))" 2>/dev/null || echo "0")
  echo "Encontradas $CONV_COUNT conversões em $ASSET_CONVERSIONS_FILE"

  python3 -c "
import json
with open('$ASSET_CONVERSIONS_FILE') as f:
    for c in json.load(f):
        print(json.dumps(c))
" | while IFS= read -r conversion_json; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/asset-conversions" \
      -H "Content-Type: application/json" \
      -d "$conversion_json" 2>/dev/null)
    if [ "$STATUS" != "200" ]; then
      OLD_TICKER=$(echo "$conversion_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('oldTicker','?'))" 2>/dev/null)
      NEW_TICKER=$(echo "$conversion_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('newTicker','?'))" 2>/dev/null)
      echo "  FAIL ($STATUS): $OLD_TICKER -> $NEW_TICKER"
    fi
  done

  echo "Conversões/incorporações processadas."
fi

# --- 9. Aguardar processamento assíncrono ---

echo ""
echo "=== Aguardando processamento assíncrono ==="
sleep 10

# --- 10. Resultado ---

echo ""
echo "=== Resultado ==="

docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "
  db = db.getSiblingDB('$DB_NAME');
  print('trading_notes:         ' + db.trading_notes.countDocuments());
  print('portfolio_events:      ' + db.portfolio_events.countDocuments());
  print('position_impact_events: ' + db.position_impact_events.countDocuments());
  print('asset_positions:       ' + db.asset_positions.countDocuments());
  print('asset_position_history: ' + db.asset_position_history.countDocuments());
  print('');
  print('--- Eventos por tipo ---');
  db.portfolio_events.aggregate([
    {\$group: {_id: '\$eventType', count: {\$sum: 1}}},
    {\$sort: {_id: 1}}
  ]).forEach(doc => print('  ' + doc._id + ': ' + doc.count));
  print('');
  print('--- Posições por tipo de ativo ---');
  db.asset_positions.aggregate([
    {\$group: {_id: '\$assetType', count: {\$sum: 1}}},
    {\$sort: {_id: 1}}
  ]).forEach(doc => print('  ' + (doc._id || 'sem tipo') + ': ' + doc.count));
"

# Verificar DLQ
echo ""
DLQ_MSGS=$(docker exec "$RABBIT_CONTAINER" rabbitmqctl list_queues name messages 2>/dev/null | grep "assetposition.calculated.dlq" | awk '{print $2}')
if [ "${DLQ_MSGS:-0}" = "0" ]; then
  echo "DLQ: vazia (nenhuma falha)"
else
  echo "DLQ: $DLQ_MSGS mensagens (verificar falhas)"
fi

echo ""
echo "Para testar replay completo: ./scripts/replay-position-impacts.sh"
echo "Carga completa."
