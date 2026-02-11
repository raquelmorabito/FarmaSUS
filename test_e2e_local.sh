#!/usr/bin/env bash
set -euo pipefail

MED_HOST="${MED_HOST:-http://localhost:8080}"
SAFE_HOST="${SAFE_HOST:-http://localhost:8081}"
RABBIT_API="${RABBIT_API:-http://localhost:15673/api}"
RABBIT_USER="${RABBIT_USER:-farma}"
RABBIT_PASS="${RABBIT_PASS:-farma}"
PG_CONTAINER="${PG_CONTAINER:-farma-postgres-safety}"
PG_USER="${PG_USER:-farma}"
PG_DB="${PG_DB:-safetydb}"

HOJE="$(date +%F)"
FIM_30="$(date -d "$HOJE + 30 day" +%F)"
FIM_7="$(date -d "$HOJE + 7 day" +%F)"

require_cmd() { command -v "$1" >/dev/null 2>&1; }
require_port() { ss -ltn | awk '{print $4}' | grep -qE ":${1}$"; }

http_code() { curl -s -o /dev/null -w "%{http_code}" "$@"; }

rabbit_messages() {
  curl -s -u "$RABBIT_USER:$RABBIT_PASS" "$RABBIT_API/queues/%2F/$1" | tr -d '\n' | sed -nE 's/.*"messages":([0-9]+).*/\1/p'
}

require_cmd ss
require_cmd curl
require_cmd docker
require_cmd awk
require_cmd sed
require_cmd grep
require_cmd date

echo "1) Healthcheck ports"
require_port 8080
require_port 8081

echo "2) Reset Rabbit queues"
curl -s -u "$RABBIT_USER:$RABBIT_PASS" -X DELETE "$RABBIT_API/queues/%2F/safety.medication.created/contents" >/dev/null || true
curl -s -u "$RABBIT_USER:$RABBIT_PASS" -X DELETE "$RABBIT_API/queues/%2F/safety.medication.created.dlq/contents" >/dev/null || true

echo "3) Reset DB"
docker exec -i "$PG_CONTAINER" psql -U "$PG_USER" -d "$PG_DB" -c "truncate table alertas_interacao restart identity;" >/dev/null
docker exec -i "$PG_CONTAINER" psql -U "$PG_USER" -d "$PG_DB" -c "truncate table prescricoes_paciente restart identity;" >/dev/null

echo "4) Assert queues empty"
[ "$(rabbit_messages "safety.medication.created")" = "0" ]
[ "$(rabbit_messages "safety.medication.created.dlq")" = "0" ]

echo "5) Create prescriptions"
C1="$(http_code -X POST "$MED_HOST/pacientes/1/medicamentos" -H "Content-Type: application/json" -d "{\"nomeMedicamento\":\"Varfarina\",\"dosagem\":\"5mg\",\"frequenciaDiaria\":1,\"horarios\":[\"08:00\"],\"inicio\":\"$HOJE\",\"fim\":\"$FIM_30\"}")"
[ "$C1" = "201" ]

C2="$(http_code -X POST "$MED_HOST/pacientes/1/medicamentos" -H "Content-Type: application/json" -d "{\"nomeMedicamento\":\"Ibuprofeno\",\"dosagem\":\"400mg\",\"frequenciaDiaria\":2,\"horarios\":[\"08:00\",\"20:00\"],\"inicio\":\"$HOJE\",\"fim\":\"$FIM_7\"}")"
[ "$C2" = "201" ]

sleep 2

echo "6) Assert queue drained and DLQ empty"
[ "$(rabbit_messages "safety.medication.created")" = "0" ]
[ "$(rabbit_messages "safety.medication.created.dlq")" = "0" ]

echo "7) Assert DB counts"
ALERTAS="$(docker exec -i "$PG_CONTAINER" psql -U "$PG_USER" -d "$PG_DB" -tA -c "select count(*) from alertas_interacao;")"
PRESCR="$(docker exec -i "$PG_CONTAINER" psql -U "$PG_USER" -d "$PG_DB" -tA -c "select count(*) from prescricoes_paciente;")"
[ "$PRESCR" = "2" ]
[ "$ALERTAS" = "1" ]

echo "8) Assert API response"
RESP="$(curl -s "$SAFE_HOST/alertas/paciente/1")"
echo "$RESP" | grep -q '"severidade":"ALTA"'

echo "OK"
