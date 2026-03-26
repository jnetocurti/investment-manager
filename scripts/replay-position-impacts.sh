#!/bin/bash
set -euo pipefail

MONGO_CONTAINER="investmentmanager-mongodb"
RABBIT_CONTAINER="investmentmanager-rabbitmq"
DB_NAME="investmentmanager"

# Cenário de replay completo:
# 1) limpa projeções de posição
# 2) republica impactos existentes na fila de impacto

echo "=== Limpando projeções de posição para replay ==="
docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "
  db = db.getSiblingDB('$DB_NAME');
  db.asset_positions.drop();
  db.asset_position_history.drop();
  print('asset_positions e asset_position_history removidas');
"

echo "=== Publicando impactos novamente na fila ==="
TMP_FILE=$(mktemp)
docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "
  db = db.getSiblingDB('$DB_NAME');
  db.position_impact_events.find().forEach(doc => print(JSON.stringify(doc)));
" > "$TMP_FILE"

while IFS= read -r line; do
  [ -z "$line" ] && continue
  PAYLOAD=$(echo "$line" | python3 -c 'import json,sys; d=json.loads(sys.stdin.read()); d.pop("_id",None); print(json.dumps(d))')
  docker exec "$RABBIT_CONTAINER" rabbitmqadmin publish \
    exchange=portfolioevent.exchange \
    routing_key=portfolioevent.impact.created \
    payload="$PAYLOAD" \
    payload_encoding=string >/dev/null
  echo "Replayed: $(echo "$PAYLOAD" | python3 -c 'import json,sys; print(json.load(sys.stdin)["originalEventId"])')"
done < "$TMP_FILE"

rm -f "$TMP_FILE"
echo "Replay concluído."
