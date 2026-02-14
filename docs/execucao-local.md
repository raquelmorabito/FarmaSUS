# Execucao Local FarmaSUS

## Fluxo recomendado (1 comando)

Use o script abaixo para:
- gerar variaveis obrigatorias automaticamente;
- subir a stack Docker;
- esperar cada servico ficar saudavel;
- salvar credenciais geradas para testes em `.env.local.generated`.

```bash
./run_local.sh
```

Quando terminar com sucesso, o script imprime:
- `AUTH_LOGIN`
- `AUTH_SENHA`
- `AUTH_TIPO`

Esses valores devem ser usados no E2E.

## Rodar teste E2E apos subir

```bash
source .env.local.generated
AUTH_LOGIN="$AUTH_SEED_PROF_LOGIN" \
AUTH_SENHA="$AUTH_SEED_PROF_SENHA" \
AUTH_TIPO="$AUTH_SEED_PROF_TIPO" \
PACIENTE_ID="$SEED_PACIENTE_ID_1" \
./test_e2e_local.sh
```

Esperado no final: `E2E OK`.

## Validacao completa (opcional)

```bash
./validate_all.sh
```

Variantes:

```bash
RUN_MVN_TESTS=1 ./validate_all.sh
KEEP_UP=1 ./validate_all.sh
RUN_MVN_TESTS=1 KEEP_UP=1 ./validate_all.sh
```

## RabbitMQ

- UI: `http://localhost:15673`
- usuario: `farma`
- senha: `farma`
- broker: `localhost:5673`

Checks:

```bash
curl -u farma:farma http://localhost:15673/api/overview
curl -u farma:farma http://localhost:15673/api/queues
```

## Health dos servicos

```bash
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8081/actuator/health
curl -i http://localhost:8082/actuator/health
curl -i http://localhost:8083/actuator/health
curl -i http://localhost:8084/actuator/health
```

Observacao: imediatamente apos `docker compose up`, os servicos ainda estao inicializando. Use `run_local.sh` para evitar falsos erros de conexao.

## Swagger

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`

## Se der erro

1. Veja status:

```bash
docker compose -f infra/docker-compose.yml ps
```

2. Veja logs do servico com problema:

```bash
docker logs --tail 200 farma-medication-service
docker logs --tail 200 farma-safety-service
docker logs --tail 200 farma-patient-service
docker logs --tail 200 farma-auth-service
docker logs --tail 200 farma-adherence-service
```

3. Erro de variavel obrigatoria (`is required`):
- rode novamente `./run_local.sh` (ele gera e injeta tudo automaticamente).

4. Erro de Rabbit/placeholder:
- confirme que esta usando o commit que corrige prefixo de propriedades RabbitMQ.

## Encerrar ambiente

```bash
docker compose -f infra/docker-compose.yml down -v
```
