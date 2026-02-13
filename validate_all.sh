#!/usr/bin/env bash
set -euo pipefail

SCRIPT_VERSION="${SCRIPT_VERSION:-1.0.0}"
COMPOSE_FILE="${COMPOSE_FILE:-infra/docker-compose.yml}"
RUN_MVN_TESTS="${RUN_MVN_TESTS:-0}"
KEEP_UP="${KEEP_UP:-0}"
JWT_SECRET="${JWT_SECRET:-}"

AUTH_LOGIN="${AUTH_LOGIN:-prof1}"
AUTH_SENHA="${AUTH_SENHA:-senha123}"
AUTH_TIPO="${AUTH_TIPO:-PROFISSIONAL}"

ts() { date '+%Y-%m-%d %H:%M:%S'; }
log() { echo "[$(ts)] $*"; }
fail() { log "ERRO: $*"; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Dependencia nao encontrada: $1"
}

cleanup() {
  if [[ "$KEEP_UP" == "1" ]]; then
    log "KEEP_UP=1 -> mantendo ambiente em execucao."
    return
  fi
  log "Derrubando ambiente Docker Compose..."
  docker compose -f "$COMPOSE_FILE" down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

need_cmd docker
need_cmd mvn
need_cmd curl
need_cmd python3

if [[ -z "$JWT_SECRET" ]]; then
  need_cmd openssl
  JWT_SECRET="$(openssl rand -base64 48)"
  log "JWT_SECRET nao informada. Chave forte gerada automaticamente para esta execucao."
fi

log "Iniciando validacao completa FarmaSUS v$SCRIPT_VERSION"
log "Subindo ambiente via Docker Compose..."
JWT_SECRET="$JWT_SECRET" docker compose -f "$COMPOSE_FILE" up -d --build

log "Rodando E2E..."
AUTH_LOGIN="$AUTH_LOGIN" AUTH_SENHA="$AUTH_SENHA" AUTH_TIPO="$AUTH_TIPO" ./test_e2e_local.sh

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
