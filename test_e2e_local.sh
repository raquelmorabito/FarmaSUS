#!/usr/bin/env bash
set -euo pipefail

SCRIPT_VERSION="${SCRIPT_VERSION:-1.0.0}"

AUTH_URL="${AUTH_URL:-http://localhost:8083}"
MEDICATION_URL="${MEDICATION_URL:-http://localhost:8080}"
SAFETY_URL="${SAFETY_URL:-http://localhost:8081}"
PATIENT_URL="${PATIENT_URL:-http://localhost:8082}"
ADHERENCE_URL="${ADHERENCE_URL:-http://localhost:8084}"

AUTH_LOGIN="${AUTH_LOGIN:-${AUTH_SEED_PROF_LOGIN:-}}"
AUTH_SENHA="${AUTH_SENHA:-${AUTH_SEED_PROF_SENHA:-}}"
AUTH_TIPO="${AUTH_TIPO:-${AUTH_SEED_PROF_TIPO:-PROFISSIONAL}}"
PACIENTE_ID="${PACIENTE_ID:-${SEED_PACIENTE_ID_1:-}}"

TMP_DIR="$(mktemp -d /tmp/farmasus-e2e.XXXXXX)"

ts() { date '+%Y-%m-%d %H:%M:%S'; }
log() { echo "[$(ts)] $*"; }
fail() {
  local msg="$1"
  echo "[$(ts)] ERRO: $msg" >&2
  echo "E2E FALHOU"
  exit 1
}

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Dependencia obrigatoria nao encontrada: $1"
}

json_field() {
  local file="$1"
  local field="$2"
  python3 - "$file" "$field" <<'PY'
import json
import sys

path = sys.argv[1]
field = sys.argv[2]
try:
    with open(path, encoding="utf-8") as fh:
        data = json.load(fh)
except Exception:
    print("")
    sys.exit(0)
value = data
for key in field.split("."):
    if isinstance(value, dict) and key in value:
        value = value[key]
    else:
        print("")
        sys.exit(0)
if value is None:
    print("")
elif isinstance(value, bool):
    print("true" if value else "false")
else:
    print(str(value))
PY
}

check_http_2xx() {
  local name="$1"
  local url="$2"
  local retries="${3:-30}"
  local wait_s="${4:-2}"
  local body_file="$TMP_DIR/check-body.json"
  local code="000"
  local body=""
  for ((i=1; i<=retries; i++)); do
    code="$(curl -sS -o "$body_file" -w '%{http_code}' "$url" || true)"
    if [[ "$code" =~ ^2 ]]; then
      log "$name OK (HTTP $code)"
      return 0
    fi
    sleep "$wait_s"
  done
  body="$(cat "$body_file" 2>/dev/null || true)"
  fail "$name indisponivel em $url. HTTP $code. Body: $body"
}

check_health_with_fallback() {
  local service="$1"
  local base_url="$2"
  local health_url="$base_url/actuator/health"
  local docs_url="$base_url/v3/api-docs"
  local body_file="$TMP_DIR/${service}-health.json"
  local code

  code="$(curl -sS -o "$body_file" -w '%{http_code}' "$health_url" || true)"
  if [[ "$code" =~ ^2 ]]; then
    log "$service health OK (HTTP $code)"
    return 0
  fi

  log "$service health nao retornou 2xx (HTTP $code). Validando disponibilidade por /v3/api-docs"
  check_http_2xx "$service /v3/api-docs" "$docs_url"
}

post_with_status() {
  local url="$1"
  local payload="$2"
  local auth_header="${3:-}"
  local out_file="$4"
  if [[ -n "$auth_header" ]]; then
    curl -sS -o "$out_file" -w '%{http_code}' -X POST "$url" -H 'Content-Type: application/json' -H "$auth_header" -d "$payload"
  else
    curl -sS -o "$out_file" -w '%{http_code}' -X POST "$url" -H 'Content-Type: application/json' -d "$payload"
  fi
}

get_with_status() {
  local url="$1"
  local auth_header="${2:-}"
  local out_file="$3"
  if [[ -n "$auth_header" ]]; then
    curl -sS -o "$out_file" -w '%{http_code}' -H "$auth_header" "$url"
  else
    curl -sS -o "$out_file" -w '%{http_code}' "$url"
  fi
}

need_cmd curl
need_cmd python3
need_cmd date

[[ -n "$AUTH_LOGIN" ]] || fail "AUTH_LOGIN nao informado."
[[ -n "$AUTH_SENHA" ]] || fail "AUTH_SENHA nao informado."
[[ -n "$PACIENTE_ID" ]] || fail "PACIENTE_ID nao informado."

log "Iniciando E2E FarmaSUS v$SCRIPT_VERSION"
log "Validando disponibilidade dos servicos"
check_health_with_fallback "medication-service" "$MEDICATION_URL"
check_health_with_fallback "safety-service" "$SAFETY_URL"
check_health_with_fallback "patient-service" "$PATIENT_URL"
check_health_with_fallback "auth-service" "$AUTH_URL"
check_health_with_fallback "adherence-service" "$ADHERENCE_URL"

log "Realizando login no auth-service com AUTH_LOGIN=$AUTH_LOGIN e AUTH_TIPO=$AUTH_TIPO"
LOGIN_PAYLOAD="$(printf '{"login":"%s","senha":"%s","tipoUsuario":"%s"}' "$AUTH_LOGIN" "$AUTH_SENHA" "$AUTH_TIPO")"
LOGIN_BODY="$TMP_DIR/login.json"
LOGIN_CODE="$(post_with_status "$AUTH_URL/auth/login" "$LOGIN_PAYLOAD" "" "$LOGIN_BODY")"
if [[ "$LOGIN_CODE" != "200" ]]; then
  fail "Falha no login. HTTP $LOGIN_CODE. Body: $(cat "$LOGIN_BODY")"
fi

TOKEN="$(json_field "$LOGIN_BODY" "token")"
LOGIN_TIPO="$(json_field "$LOGIN_BODY" "tipoUsuario")"
[[ -n "$TOKEN" ]] || fail "Token ausente na resposta de /auth/login. Body: $(cat "$LOGIN_BODY")"
[[ "$LOGIN_TIPO" == "$AUTH_TIPO" ]] || fail "tipoUsuario de /auth/login divergente. Esperado=$AUTH_TIPO Recebido=$LOGIN_TIPO"
log "Login validado e token JWT obtido"

log "Validando /auth/me com Bearer token"
ME_BODY="$TMP_DIR/me.json"
ME_CODE="$(get_with_status "$AUTH_URL/auth/me" "Authorization: Bearer $TOKEN" "$ME_BODY")"
if [[ "$ME_CODE" != "200" ]]; then
  fail "Falha em /auth/me. HTTP $ME_CODE. Body: $(cat "$ME_BODY")"
fi
ME_LOGIN="$(json_field "$ME_BODY" "login")"
ME_TIPO="$(json_field "$ME_BODY" "tipoUsuario")"
[[ "$ME_LOGIN" == "$AUTH_LOGIN" ]] || fail "login de /auth/me divergente. Esperado=$AUTH_LOGIN Recebido=$ME_LOGIN"
[[ "$ME_TIPO" == "$AUTH_TIPO" ]] || fail "tipoUsuario de /auth/me divergente. Esperado=$AUTH_TIPO Recebido=$ME_TIPO"
log "/auth/me validado com sucesso"

HOJE="$(date +%F)"
FIM="$(date -d '+7 days' +%F)"
PRESCRICAO_PAYLOAD="$(printf '{"nomeMedicamento":"Dipirona","dosagem":"500mg","frequenciaDiaria":2,"horarios":["08:00","20:00"],"inicio":"%s","fim":"%s"}' "$HOJE" "$FIM")"

log "Validando endpoint protegido sem token (esperado 401)"
MED_UNAUTH_BODY="$TMP_DIR/medication-unauth.json"
MED_UNAUTH_CODE="$(post_with_status "$MEDICATION_URL/pacientes/$PACIENTE_ID/medicamentos" "$PRESCRICAO_PAYLOAD" "" "$MED_UNAUTH_BODY")"
if [[ "$MED_UNAUTH_CODE" != "401" ]]; then
  fail "Endpoint protegido sem token nao retornou 401. HTTP $MED_UNAUTH_CODE. Body: $(cat "$MED_UNAUTH_BODY")"
fi
log "Sem token retornou 401 como esperado"

log "Validando endpoint protegido com token no medication-service"
MED_AUTH_BODY="$TMP_DIR/medication-auth.json"
MED_AUTH_CODE="$(post_with_status "$MEDICATION_URL/pacientes/$PACIENTE_ID/medicamentos" "$PRESCRICAO_PAYLOAD" "Authorization: Bearer $TOKEN" "$MED_AUTH_BODY")"

if [[ "$MED_AUTH_CODE" == "401" ]]; then
  echo "[$(ts)] DEBUG medication-service retornou 401 com token" >&2
  echo "[$(ts)] DEBUG status=$MED_AUTH_CODE" >&2
  echo "[$(ts)] DEBUG body=$(cat "$MED_AUTH_BODY")" >&2
  echo "[$(ts)] DEBUG proximas checagens:" >&2
  echo "[$(ts)] DEBUG 1) JWT_SECRET igual em auth-service e medication-service" >&2
  echo "[$(ts)] DEBUG 2) algoritmo de assinatura esperado pelos dois servicos" >&2
  echo "[$(ts)] DEBUG 3) expiracao do token (claim exp) no momento da chamada" >&2
  echo "[$(ts)] DEBUG 4) claim tipoUsuario presente e em formato esperado" >&2
  fail "Token rejeitado no medication-service"
fi

if [[ "$MED_AUTH_CODE" != "200" && "$MED_AUTH_CODE" != "201" ]]; then
  fail "Falha no POST protegido do medication-service. HTTP $MED_AUTH_CODE. Body: $(cat "$MED_AUTH_BODY")"
fi

log "POST protegido no medication-service aceitou JWT (HTTP $MED_AUTH_CODE)"
echo "E2E OK"
