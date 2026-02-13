#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ ! -f "$ROOT_DIR/.gitignore" ]]; then
  touch "$ROOT_DIR/.gitignore"
fi

if ! rg -n '^target/$' "$ROOT_DIR/.gitignore" >/dev/null 2>&1; then
  printf "\ntarget/\n**/target/\n" >>"$ROOT_DIR/.gitignore"
fi

mkdir -p "$ROOT_DIR/docs"

cat >"$ROOT_DIR/docs/checklist_demo.md" <<'EOF'
Checklist de demo (FARMA-SUS-LAB)

1) Subir stack
- cd infra
- docker compose up -d --build
- docker compose ps

2) Swagger
- auth: http://localhost:8083/swagger-ui.html
- medication: http://localhost:8080/swagger-ui.html
- safety: http://localhost:8081/swagger-ui.html
- patient: http://localhost:8082/swagger-ui.html
- adherence: http://localhost:8084/swagger-ui.html

3) Fluxo E2E (sem front-end)
- login (profissional) -> token
- criar prescrição (Varfarina e Ibuprofeno)
- verificar alerta em safety
- verificar alerta agregado em patient
- listar doses do dia em adherence
- registrar dose TOMOU

4) Evidência
- print/registro das respostas HTTP e JSON
- logs se necessário: docker compose logs -n 200 <service>
EOF

cat >"$ROOT_DIR/README.md" <<'EOF'
FARMA-SUS-LAB

Objetivo
Backend em microserviços (sem front-end) para registrar prescrições e uso de medicamentos, detectar interações e gerar alertas, além de controlar doses e registros de tomada. Focado em requisitos de hackathon: microserviços, mensageria, persistência, segurança, testes e execução local via Docker Compose.

Arquitetura
Serviços:
- auth-service (8083): autenticação e emissão de JWT com perfis PACIENTE e PROFISSIONAL
- medication-service (8080): gestão de prescrições e horários
- safety-service (8081): detecção de interações e geração de alertas
- patient-service (8082): agregação de alertas por paciente
- adherence-service (8084): geração/consulta de doses do dia e registro de tomada

Infra:
- RabbitMQ (5673/15673)
- PostgreSQL por serviço

Padrão de datas e horário
- Datas: YYYY-MM-DD (ex.: 2026-02-11)
- Horários: HH:mm (ex.: 08:00)
- Fuso: America/Sao_Paulo (quando aplicável)

Como rodar localmente
1) Subir stack
cd infra
docker compose up -d --build

2) Ver status
docker compose ps

3) Swagger
- http://localhost:8083/swagger-ui.html
- http://localhost:8080/swagger-ui.html
- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html
- http://localhost:8084/swagger-ui.html

Roteiro de demo (curl)
1) Login (profissional)
curl -s -X POST http://localhost:8083/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"prof1","senha":"senha123","tipoUsuario":"PROFISSIONAL"}'

2) Criar prescrição (exemplo)
TOKEN="<cole_o_token_aqui>"

curl -s -X POST http://localhost:8080/pacientes/1/medicamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nomeMedicamento":"Varfarina","dosagem":"5mg","frequenciaDiaria":1,"horarios":["08:00"],"inicio":"2026-02-11","fim":"2026-02-18"}'

curl -s -X POST http://localhost:8080/pacientes/1/medicamentos \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nomeMedicamento":"Ibuprofeno","dosagem":"400mg","frequenciaDiaria":2,"horarios":["10:00","22:00"],"inicio":"2026-02-11","fim":"2026-02-18"}'

3) Validar alertas
curl -s http://localhost:8081/alertas/paciente/1
curl -s http://localhost:8082/pacientes/1/alertas -H "Authorization: Bearer $TOKEN"

4) Doses e registro de tomada
curl -s http://localhost:8084/pacientes/1/doses-hoje -H "Authorization: Bearer $TOKEN"

DOSE_ID="<cole_um_id_de_dose>"

curl -s -X POST http://localhost:8084/pacientes/1/doses/$DOSE_ID/registro \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"TOMOU","registradoEm":"2026-02-11T18:45:00Z"}'

Script E2E
Na raiz do repositório:
./test_e2e_local.sh
./test_e2e_local.sh --down

Encerrar
cd infra
docker compose down
EOF

mkdir -p "$ROOT_DIR/.github/workflows"

cat >"$ROOT_DIR/.github/workflows/ci.yml" <<'EOF'
name: ci
on:
  push:
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - name: Build services
        run: |
          for d in services/*; do
            if [ -f "$d/pom.xml" ]; then
              (cd "$d" && mvn -q -DskipTests package)
            fi
          done
EOF

echo "OK: README.md + docs/checklist_demo.md + .gitignore + workflow CI criados/atualizados"
