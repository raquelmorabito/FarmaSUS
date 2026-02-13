#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$ROOT_DIR/infra"
COMPOSE_FILE="$INFRA_DIR/docker-compose.yml"
ENV_FILE="$INFRA_DIR/.env"

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "ERRO: nao encontrei $COMPOSE_FILE"
  exit 1
fi

mkdir -p "$INFRA_DIR"

if [[ ! -f "$ENV_FILE" ]]; then
  JWT_SECRET_VALUE="dev-jwt-secret-$(date +%s)"
  cat >"$ENV_FILE" <<EOF
JWT_SECRET=$JWT_SECRET_VALUE
EOF
  echo "Criado $ENV_FILE com JWT_SECRET"
else
  if ! grep -q '^JWT_SECRET=' "$ENV_FILE"; then
    JWT_SECRET_VALUE="dev-jwt-secret-$(date +%s)"
    printf "\nJWT_SECRET=%s\n" "$JWT_SECRET_VALUE" >>"$ENV_FILE"
    echo "Adicionado JWT_SECRET em $ENV_FILE"
  fi
fi

patch_compose_envfile() {
  local svc="$1"
  if ! rg -n "^[[:space:]]*$svc:[[:space:]]*$" "$COMPOSE_FILE" >/dev/null 2>&1; then
    echo "Aviso: service $svc nao encontrado no compose"
    return 0
  fi

  if rg -n "^[[:space:]]*$svc:[[:space:]]*$" "$COMPOSE_FILE" -n >/dev/null 2>&1; then
    :
  fi

  if rg -n "^[[:space:]]*$svc:[[:space:]]*$" "$COMPOSE_FILE" >/dev/null 2>&1; then
    if ! awk -v svc="$svc" '
      $0 ~ "^[[:space:]]*"svc":[[:space:]]*$" {in=1; next}
      in==1 && $0 ~ "^[[:space:]]*[a-zA-Z0-9_-]+:[[:space:]]*$" {exit}
      in==1 && $0 ~ "^[[:space:]]*env_file:[[:space:]]*$" {found=1}
      END {exit(found?0:1)}
    ' "$COMPOSE_FILE"; then
      perl -0777 -i -pe "s/(\n[ ]*$svc:\n)([ ]{2,}.*\n)/\$1  env_file:\n    - .\/.env\n\$2/" "$COMPOSE_FILE"
      echo "Compose: env_file adicionado em $svc"
    fi
  fi
}

patch_compose_envfile "auth-service"
patch_compose_envfile "medication-service"
patch_compose_envfile "patient-service"
patch_compose_envfile "adherence-service"
patch_compose_envfile "safety-service"

patch_app_docker_yaml() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    echo "Aviso: nao encontrei $file"
    return 0
  fi

  perl -i -pe 's/app\.jwt\.secret:\s*\$\{jwt\.secret\}/app:\n  jwt:\n    secret: ${JWT_SECRET:dev-jwt-secret}/g' "$file"
  perl -i -pe 's/app\.jwt\.secret:\s*\$\{jwt\.secret:([^}]*)\}/app:\n  jwt:\n    secret: ${JWT_SECRET:$1}/g' "$file"
  perl -i -pe 's/app\.jwt\.secret:\s*.*\$\{jwt\.secret\}.*$/app:\n  jwt:\n    secret: ${JWT_SECRET:dev-jwt-secret}/g' "$file"

  if ! rg -n "^[[:space:]]*app:[[:space:]]*$" "$file" >/dev/null 2>&1; then
    printf "\napp:\n  jwt:\n    secret: \${JWT_SECRET:dev-jwt-secret}\n" >>"$file"
  else
    if ! rg -n "^[[:space:]]*jwt:[[:space:]]*$" "$file" >/dev/null 2>&1; then
      perl -0777 -i -pe 's/\napp:\n/\napp:\n  jwt:\n    secret: ${JWT_SECRET:dev-jwt-secret}\n/' "$file"
    else
      if ! rg -n "secret:[[:space:]]*\$\{JWT_SECRET" "$file" >/dev/null 2>&1; then
        perl -0777 -i -pe 's/\n[ ]*jwt:\n/\n  jwt:\n    secret: ${JWT_SECRET:dev-jwt-secret}\n/' "$file"
      fi
    fi
  fi
}

patch_app_docker_yaml "$ROOT_DIR/services/auth-service/src/main/resources/application-docker.yml"
patch_app_docker_yaml "$ROOT_DIR/services/medication-service/src/main/resources/application-docker.yml"
patch_app_docker_yaml "$ROOT_DIR/services/patient-service/src/main/resources/application-docker.yml"
patch_app_docker_yaml "$ROOT_DIR/services/adherence-service/src/main/resources/application-docker.yml"

add_actuator_dep() {
  local pom="$1"
  if [[ ! -f "$pom" ]]; then
    echo "Aviso: nao encontrei $pom"
    return 0
  fi
  if rg -n "spring-boot-starter-actuator" "$pom" >/dev/null 2>&1; then
    return 0
  fi
  perl -0777 -i -pe 's/(<dependencies>\s*)/$1\n    <dependency>\n      <groupId>org.springframework.boot<\/groupId>\n      <artifactId>spring-boot-starter-actuator<\/artifactId>\n    <\/dependency>\n/s' "$pom"
  echo "Actuator adicionado em $pom"
}

add_actuator_dep "$ROOT_DIR/services/safety-service/pom.xml"
add_actuator_dep "$ROOT_DIR/services/auth-service/pom.xml"
add_actuator_dep "$ROOT_DIR/services/adherence-service/pom.xml"
add_actuator_dep "$ROOT_DIR/services/medication-service/pom.xml"
add_actuator_dep "$ROOT_DIR/services/patient-service/pom.xml"

patch_actuator_exposure() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    return 0
  fi
  if rg -n "management:\s*$" "$file" >/dev/null 2>&1; then
    if ! rg -n "management\.endpoints\.web\.exposure\.include" "$file" >/dev/null 2>&1; then
      cat >>"$file" <<'EOF'

management:
  endpoints:
    web:
      exposure:
        include: health,info
EOF
    fi
  else
    cat >>"$file" <<'EOF'

management:
  endpoints:
    web:
      exposure:
        include: health,info
EOF
  fi
}

for f in "$ROOT_DIR/services/"*/src/main/resources/application-docker.yml; do
  patch_actuator_exposure "$f"
done

patch_security_config() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    echo "Aviso: nao encontrei $file"
    return 0
  fi

  if rg -n '"/swagger-ui' "$file" >/dev/null 2>&1; then
    :
  else
    perl -0777 -i -pe 's/requestMatchers\(\s*/requestMatchers(\n                "\/swagger-ui\/",\n                "\/swagger-ui\/**",\n                "\/v3\/api-docs\/",\n                "\/v3\/api-docs\/**",\n                "\/actuator\/health",\n                "\/actuator\/health\/**",\n                /s' "$file"
  fi
}

patch_security_config "$ROOT_DIR/services/auth-service/src/main/java/br/gov/farmasus/auth/config/SecurityConfig.java"
patch_security_config "$ROOT_DIR/services/medication-service/src/main/java/br/gov/farmasus/medication/config/SecurityConfig.java"
patch_security_config "$ROOT_DIR/services/patient-service/src/main/java/br/gov/farmasus/patient/config/SecurityConfig.java"
patch_security_config "$ROOT_DIR/services/adherence-service/src/main/java/br/gov/farmasus/adherence/config/SecurityConfig.java"

echo "Subindo stack"
if docker compose version >/dev/null 2>&1; then
  docker compose -f "$COMPOSE_FILE" up -d --build
else
  docker-compose -f "$COMPOSE_FILE" up -d --build
fi

echo "Status"
if docker compose version >/dev/null 2>&1; then
  docker compose -f "$COMPOSE_FILE" ps
else
  docker-compose -f "$COMPOSE_FILE" ps
fi

echo "Health endpoints (HTTP code)"
for p in 8080 8081 8082 8083 8084; do
  code="$(curl -s -o /tmp/health_$p.json -w '%{http_code}' "http://127.0.0.1:$p/actuator/health" || true)"
  echo "127.0.0.1:$p/actuator/health -> $code"
done
