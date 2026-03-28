#!/bin/bash
#
# Carga completa: notas de negociação + subscrições + ações corporativas
#
# Uso:
#   ./scripts/load-data.sh <diretório-notas> [arquivo-subscricoes.json] [arquivo-corporate-actions.json]
#
# Exemplos:
#   ./scripts/load-data.sh ~/notas_negociacao
#   ./scripts/load-data.sh ~/notas_negociacao subscricoes.json corporate-actions.json

set -euo pipefail

API_BASE="http://localhost:8080"
MONGO_CONTAINER="investmentmanager-mongodb"
RABBIT_CONTAINER="investmentmanager-rabbitmq"
DB_NAME="investmentmanager"

# --- Validação de argumentos ---

NOTES_DIR="${1:-}"
SUBSCRIPTIONS_FILE="${2:-}"
CORPORATE_ACTIONS_FILE="${3:-}"

if [ -z "$NOTES_DIR" ]; then
  echo "Uso: $0 <diretório-notas> [arquivo-subscricoes.json] [arquivo-corporate-actions.json]"
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

if [ -n "$CORPORATE_ACTIONS_FILE" ] && [ ! -f "$CORPORATE_ACTIONS_FILE" ]; then
  echo "Erro: arquivo de ações corporativas não encontrado: $CORPORATE_ACTIONS_FILE"
  exit 1
fi

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

for queue in tradingnote.created.queue portfolioevent.processed.queue portfolioevent.impact.queue assetposition.calculated.dlq portfolioevent.created.queue; do
  docker exec "$RABBIT_CONTAINER" rabbitmqctl purge_queue "$queue" 2>/dev/null || true
done
echo "Filas limpas."

echo ""
echo "=== Verificando aplicação ==="
if wait_for_service "$API_BASE/api/trading-notes/upload"; then
  echo "Aplicação disponível em $API_BASE"
else
  echo "Erro: aplicação não está respondendo em $API_BASE"
  echo "Inicie a aplicação e tente novamente."
  exit 1
fi

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

if [ -n "$SUBSCRIPTIONS_FILE" ]; then
  echo ""
  echo "=== Enviando subscrições ==="

  SUB_COUNT=$(python3 -c "import json; print(len(json.load(open('$SUBSCRIPTIONS_FILE'))))" 2>/dev/null || echo "0")
  echo "Encontradas $SUB_COUNT subscrições em $SUBSCRIPTIONS_FILE"

  python3 -c "
import json
with open('$SUBSCRIPTIONS_FILE') as f:
    subs = json.load(f)
for s in subs:
    print(json.dumps(s))
" | while IFS= read -r sub_json; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/subscriptions" \
      -H "Content-Type: application/json" \
      -d "$sub_json" 2>/dev/null)
    if [ "$STATUS" != "200" ]; then
      TICKER=$(echo "$sub_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('subscriptionTicker','?'))" 2>/dev/null)
      echo "  FAIL ($STATUS): $TICKER"
    fi
  done

  echo "Subscrições processadas."
fi

if [ -n "$CORPORATE_ACTIONS_FILE" ]; then
  echo ""
  echo "=== Enviando ações corporativas (split/reverse split) ==="

  CA_COUNT=$(python3 -c "import json; print(len(json.load(open('$CORPORATE_ACTIONS_FILE'))))" 2>/dev/null || echo "0")
  echo "Encontradas $CA_COUNT ações corporativas em $CORPORATE_ACTIONS_FILE"

  python3 -c "
import json
with open('$CORPORATE_ACTIONS_FILE') as f:
    actions = json.load(f)
for a in actions:
    print(json.dumps(a))
" | while IFS= read -r action_json; do
    ACTION_TYPE=$(echo "$action_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('type','').lower())")
    if [ "$ACTION_TYPE" = "split" ]; then
      ENDPOINT="split"
    elif [ "$ACTION_TYPE" = "reverse_split" ]; then
      ENDPOINT="reverse-split"
    else
      echo "  SKIP tipo desconhecido: $ACTION_TYPE"
      continue
    fi

    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_BASE/api/portfolio-events/$ENDPOINT" \
      -H "Content-Type: application/json" \
      -d "$action_json" 2>/dev/null)

    if [ "$STATUS" != "200" ]; then
      TICKER=$(echo "$action_json" | python3 -c "import json,sys; print(json.load(sys.stdin).get('ticker','?'))")
      echo "  FAIL ($STATUS): $ACTION_TYPE/$TICKER"
    fi
  done

  echo "Ações corporativas processadas."
fi

echo ""
echo "=== Aguardando processamento assíncrono ==="
sleep 10

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
"

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
