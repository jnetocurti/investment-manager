#!/bin/bash
#
# Carga completa: notas de negociação + subscrições
#
# Uso:
#   ./scripts/load-data.sh <diretório-notas> [arquivo-subscricoes.json]
#
# Exemplos:
#   ./scripts/load-data.sh ~/notas_negociacao
#   ./scripts/load-data.sh ~/notas_negociacao subscricoes.json
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

# --- Validação de argumentos ---

NOTES_DIR="${1:-}"
SUBSCRIPTIONS_FILE="${2:-}"

if [ -z "$NOTES_DIR" ]; then
  echo "Uso: $0 <diretório-notas> [arquivo-subscricoes.json]"
  exit 1
fi

if [ ! -d "$NOTES_DIR" ]; then
  echo "Erro: diretório não encontrado: $NOTES_DIR"
  exit 1
fi

if [ -n "$SUBSCRIPTIONS_FILE" ] && [ ! -f "$SUBSCRIPTIONS_FILE" ]; then
  echo "Erro: arquivo de subscrições não encontrado: $SUBSCRIPTIONS_FILE"
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
  db.asset_positions.drop();
  db.asset_position_history.drop();
  print('Collections removidas.');
"

# Limpar filas (ignorar erros se não existirem)
for queue in tradingnote.created.queue portfolioevent.processed.queue assetposition.calculated.dlq portfolioevent.created.queue; do
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
  else
    FAIL=$((FAIL + 1))
    echo "  FAIL ($STATUS): $(basename "$pdf")"
  fi
done

echo "Notas: $SUCCESS ok, $SKIPPED ignorados (PDF inválido), $FAIL erros"

# --- 5. Subscrições ---

if [ -n "$SUBSCRIPTIONS_FILE" ]; then
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

# --- 6. Aguardar processamento assíncrono ---

echo ""
echo "=== Aguardando processamento assíncrono ==="
sleep 10

# --- 7. Resultado ---

echo ""
echo "=== Resultado ==="

docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "
  db = db.getSiblingDB('$DB_NAME');
  print('trading_notes:         ' + db.trading_notes.countDocuments());
  print('portfolio_events:      ' + db.portfolio_events.countDocuments());
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
echo "Carga completa."
