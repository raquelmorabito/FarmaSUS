# Inventario de Dados Chumbados - FarmaSUS

## Auth Service
- `services/auth-service/src/main/java/br/gov/farmasus/auth/service/UsuarioService.java`
  - Antes: usuarios e senhas fixos em `Map.of`.
  - Depois: removido. Usuarios persistidos em `usuarios` via JPA/Flyway.
- `services/auth-service/src/main/java/br/gov/farmasus/auth/service/AutenticacaoService.java`
  - Antes: comparacao de senha em texto puro.
  - Depois: validacao com `PasswordEncoder` (BCrypt).
- `services/auth-service/src/main/resources/application.yml`
- `services/auth-service/src/main/resources/application-docker.yml`
  - Antes: sem datasource/flyway.
  - Depois: datasource + flyway + placeholders de seed externos.
- `services/auth-service/src/main/resources/db/migration/V1__create_usuarios_table.sql`
- `services/auth-service/src/main/resources/db/migration/V2__seed_usuarios.java`
  - Seed inicial externalizado por placeholders/env.

## Medication Service
- `services/medication-service/src/main/java/br/gov/farmasus/medication/config/DockerSeedConfig.java`
  - Antes: mapeamento login->paciente fixo (`paciente1 -> 1`).
  - Depois: seed por propriedades `app.seed.paciente-login.mappings`.
- `services/medication-service/src/main/java/br/gov/farmasus/medication/service/PrescricaoService.java`
  - Antes: fallback de URL hardcoded em codigo.
  - Depois: URL somente por propriedade.
- `services/medication-service/src/main/java/br/gov/farmasus/medication/service/ReceitaPertoVencerScheduler.java`
  - Antes: routing key e defaults em anotacao/codigo.
  - Depois: routing/exchange e parametros por propriedades.
- `services/medication-service/src/main/java/br/gov/farmasus/medication/config/RabbitConfig.java`
  - Antes: exchange hardcoded em constante.
  - Depois: exchange por `rabbitmq.messaging.*`.
- `services/medication-service/src/main/java/br/gov/farmasus/medication/config/HttpClientConfig.java`
  - Antes: timeout default em codigo.
  - Depois: timeout via `http.client.*`.

## Patient Service
- `services/patient-service/src/main/java/br/gov/farmasus/patient/config/DockerSeedConfig.java`
  - Antes: seeds fixos de login/paciente.
  - Depois: seed por propriedades `app.seed.paciente-login.mappings`.
- `services/patient-service/src/main/java/br/gov/farmasus/patient/service/CartaoMedicacaoService.java`
- `services/patient-service/src/main/java/br/gov/farmasus/patient/service/LembretePacienteService.java`
  - Antes: fallback de URLs hardcoded em codigo.
  - Depois: URLs somente por propriedade.
- `services/patient-service/src/main/java/br/gov/farmasus/patient/config/RabbitConfig.java`
- `services/patient-service/src/main/java/br/gov/farmasus/patient/listener/AlertaPacienteListener.java`
  - Antes: queue/routing/exchange em constantes de codigo.
  - Depois: `rabbitmq.messaging.*` por propriedades.
- `services/patient-service/src/main/java/br/gov/farmasus/patient/config/HttpClientConfig.java`
  - Antes: timeout default em codigo.
  - Depois: timeout via `http.client.*`.

## Safety Service
- `services/safety-service/src/main/java/br/gov/farmasus/safety/config/RabbitConfig.java`
- `services/safety-service/src/main/java/br/gov/farmasus/safety/service/AlertaService.java`
- `services/safety-service/src/main/java/br/gov/farmasus/safety/listener/PrescricaoListener.java`
  - Antes: queue/routing/exchange hardcoded em constantes.
  - Depois: `rabbitmq.messaging.*` por propriedades.

## Adherence Service
- `services/adherence-service/src/main/java/br/gov/farmasus/adherence/service/PacienteLoginMapper.java`
  - Antes: mapa fixo em codigo (`paciente1`, `paciente2`).
  - Depois: mapa externalizado em `app.paciente-login.mappings`.
- `services/adherence-service/src/main/java/br/gov/farmasus/adherence/config/RabbitConfig.java`
- `services/adherence-service/src/main/java/br/gov/farmasus/adherence/listener/PrescricaoListener.java`
- `services/adherence-service/src/main/java/br/gov/farmasus/adherence/service/DoseService.java`
  - Antes: queue/routing/exchange hardcoded em constantes.
  - Depois: `rabbitmq.messaging.*` por propriedades.

## Infra e Scripts
- `infra/docker-compose.yml`
  - Antes: apenas `JWT_SECRET`; seeds distribuidos em codigo dos servicos/scripts.
  - Depois: variaveis explicitas para seeds de auth e mapeamentos de paciente.
- `test_e2e_local.sh`
  - Antes: credenciais default fixas.
  - Depois: credenciais vindas de seed/env.
- `validate_all.sh`
  - Antes: defaults fixos (`prof1/senha123`).
  - Depois: seeds e credenciais por env com geracao automatica quando ausentes.

## Observacoes
- Valores de dominio (ex.: enums `Severidade`, status `PENDENTE`) foram mantidos por serem parte do contrato de negocio e nao configuracao de ambiente.
- IDs usados em testes de integracao permanecem controlados no proprio teste/seed de teste.
