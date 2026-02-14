#!/usr/bin/env bash
set -euo pipefail

SCRIPT_VERSION="${SCRIPT_VERSION:-1.0.0}"
COMPOSE_FILE="${COMPOSE_FILE:-infra/docker-compose.yml}"
RUN_MVN_TESTS="${RUN_MVN_TESTS:-0}"
KEEP_UP="${KEEP_UP:-0}"
JWT_SECRET="${JWT_SECRET:-}"

SEED_PACIENTE_LOGIN_1="${SEED_PACIENTE_LOGIN_1:-}"
SEED_PACIENTE_ID_1="${SEED_PACIENTE_ID_1:-1}"
SEED_PACIENTE_LOGIN_2="${SEED_PACIENTE_LOGIN_2:-}"
SEED_PACIENTE_ID_2="${SEED_PACIENTE_ID_2:-2}"

AUTH_SEED_PACIENTE_LOGIN="${AUTH_SEED_PACIENTE_LOGIN:-$SEED_PACIENTE_LOGIN_1}"
AUTH_SEED_PACIENTE_SENHA="${AUTH_SEED_PACIENTE_SENHA:-}"
AUTH_SEED_PACIENTE_TIPO="${AUTH_SEED_PACIENTE_TIPO:-PACIENTE}"
AUTH_SEED_PROF_LOGIN="${AUTH_SEED_PROF_LOGIN:-}"
AUTH_SEED_PROF_SENHA="${AUTH_SEED_PROF_SENHA:-}"
AUTH_SEED_PROF_TIPO="${AUTH_SEED_PROF_TIPO:-PROFISSIONAL}"

AUTH_LOGIN_INPUT="${AUTH_LOGIN:-}"
AUTH_SENHA_INPUT="${AUTH_SENHA:-}"
AUTH_TIPO_INPUT="${AUTH_TIPO:-}"
AUTH_LOGIN_SET="${AUTH_LOGIN+x}"
AUTH_SENHA_SET="${AUTH_SENHA+x}"
AUTH_TIPO_SET="${AUTH_TIPO+x}"
PACIENTE_ID="${PACIENTE_ID:-$SEED_PACIENTE_ID_1}"

ts() { date '+%Y-%m-%d %H:%M:%S'; }
log() { echo "[$(ts)] $*"; }
fail() { log "ERRO: $*"; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Dependencia nao encontrada: $1"
}

compose_with_env() {
  JWT_SECRET="$JWT_SECRET" \
  SEED_PACIENTE_LOGIN_1="$SEED_PACIENTE_LOGIN_1" \
  SEED_PACIENTE_ID_1="$SEED_PACIENTE_ID_1" \
  SEED_PACIENTE_LOGIN_2="$SEED_PACIENTE_LOGIN_2" \
  SEED_PACIENTE_ID_2="$SEED_PACIENTE_ID_2" \
  AUTH_SEED_PACIENTE_LOGIN="$AUTH_SEED_PACIENTE_LOGIN" \
  AUTH_SEED_PACIENTE_SENHA="$AUTH_SEED_PACIENTE_SENHA" \
  AUTH_SEED_PACIENTE_TIPO="$AUTH_SEED_PACIENTE_TIPO" \
  AUTH_SEED_PROF_LOGIN="$AUTH_SEED_PROF_LOGIN" \
  AUTH_SEED_PROF_SENHA="$AUTH_SEED_PROF_SENHA" \
  AUTH_SEED_PROF_TIPO="$AUTH_SEED_PROF_TIPO" \
  docker compose -f "$COMPOSE_FILE" "$@"
}

cleanup() {
  if [[ "$KEEP_UP" == "1" ]]; then
    log "KEEP_UP=1 -> mantendo ambiente em execucao."
    return
  fi
  log "Derrubando ambiente Docker Compose..."
  compose_with_env down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

need_cmd docker
need_cmd mvn
need_cmd curl
need_cmd python3
need_cmd openssl

if [[ -z "$AUTH_SEED_PACIENTE_SENHA" ]]; then
  AUTH_SEED_PACIENTE_SENHA="$(openssl rand -hex 8)"
fi
if [[ -z "$SEED_PACIENTE_LOGIN_1" ]]; then
  SEED_PACIENTE_LOGIN_1="pac_$(date +%s)"
fi
if [[ -z "$SEED_PACIENTE_LOGIN_2" ]]; then
  SEED_PACIENTE_LOGIN_2="pac_$(date +%s)_2"
fi
AUTH_SEED_PACIENTE_LOGIN="${AUTH_SEED_PACIENTE_LOGIN:-$SEED_PACIENTE_LOGIN_1}"
if [[ -z "$AUTH_SEED_PROF_LOGIN" ]]; then
  AUTH_SEED_PROF_LOGIN="prof_$(date +%s)"
fi
if [[ -z "$AUTH_SEED_PROF_SENHA" ]]; then
  AUTH_SEED_PROF_SENHA="$(openssl rand -hex 8)"
fi

if [[ -z "${AUTH_LOGIN_SET:-}" && -z "${AUTH_SENHA_SET:-}" && -z "${AUTH_TIPO_SET:-}" ]]; then
  AUTH_LOGIN="$AUTH_SEED_PROF_LOGIN"
  AUTH_SENHA="$AUTH_SEED_PROF_SENHA"
  AUTH_TIPO="$AUTH_SEED_PROF_TIPO"
else
  [[ -n "$AUTH_LOGIN_INPUT" ]] || fail "AUTH_LOGIN foi informada parcialmente. Informe AUTH_LOGIN, AUTH_SENHA e AUTH_TIPO juntos."
  [[ -n "$AUTH_SENHA_INPUT" ]] || fail "AUTH_SENHA foi informada parcialmente. Informe AUTH_LOGIN, AUTH_SENHA e AUTH_TIPO juntos."
  [[ -n "$AUTH_TIPO_INPUT" ]] || fail "AUTH_TIPO foi informada parcialmente. Informe AUTH_LOGIN, AUTH_SENHA e AUTH_TIPO juntos."
  AUTH_LOGIN="$AUTH_LOGIN_INPUT"
  AUTH_SENHA="$AUTH_SENHA_INPUT"
  AUTH_TIPO="$AUTH_TIPO_INPUT"
fi

if [[ -z "$JWT_SECRET" ]]; then
  JWT_SECRET="$(openssl rand -base64 48)"
  log "JWT_SECRET nao informada. Chave forte gerada automaticamente para esta execucao."
fi

log "Iniciando validacao completa FarmaSUS v$SCRIPT_VERSION"
log "Credenciais E2E em uso: AUTH_LOGIN=$AUTH_LOGIN AUTH_TIPO=$AUTH_TIPO"
log "Limpando stack anterior..."
compose_with_env down -v >/dev/null 2>&1 || true
log "Subindo ambiente via Docker Compose..."
compose_with_env up -d --build

log "Rodando E2E..."
AUTH_LOGIN="$AUTH_LOGIN" \
AUTH_SENHA="$AUTH_SENHA" \
AUTH_TIPO="$AUTH_TIPO" \
PACIENTE_ID="$PACIENTE_ID" \
AUTH_SEED_PROF_LOGIN="$AUTH_SEED_PROF_LOGIN" \
AUTH_SEED_PROF_SENHA="$AUTH_SEED_PROF_SENHA" \
SEED_PACIENTE_ID_1="$SEED_PACIENTE_ID_1" \
./test_e2e_local.sh

if [[ "$RUN_MVN_TESTS" == "1" ]]; then
  log "RUN_MVN_TESTS=1 -> executando testes Maven principais..."
  mvn -B -f services/medication-service/pom.xml test
  mvn -B -f services/patient-service/pom.xml test
  mvn -B -f services/adherence-service/pom.xml test
else
  log "RUN_MVN_TESTS=0 -> pulando testes Maven (somente E2E executado)."
fi

log "Checklist final:"
log "1) E2E OK"
log "2) Auth/login e endpoint protegido validados"
log "3) Ambiente Docker operacional"
log "Validacao concluida com sucesso."
