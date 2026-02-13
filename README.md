# FarmaSUS MVP

MVP desenvolvido para apoiar o objetivo da pos-graduacao: criar sistemas, ferramentas ou plataformas tecnologicas que melhorem o atendimento a populacao e o trabalho dos profissionais de saude, com foco em eficiencia, transparencia e melhor experiencia para pacientes e colaboradores do SUS.

## Pitch
O FarmaSUS transforma dados clinicos em acao coordenada.  
Com autenticacao segura, prescricoes rastreaveis, alertas de risco e visao do paciente em tempo real, o projeto reduz ruído operacional, aumenta seguranca assistencial e oferece uma base pronta para evolucao em escala no ecossistema SUS.

## Personas
### Paciente
**Maria Silva, 62 anos, hipertensa e diabetica, usuaria da UBS do bairro**  
Maria usa varios medicamentos, passa por consultas com profissionais diferentes e quase sempre esquece nomes, doses e horarios. Ela precisa de um cartao de medicacao unico, confiavel e facil de consultar para responder rapidamente "quais remedios voce toma?", receber lembretes de tratamento e descobrir onde encontrar seu medicamento na rede.

### Profissional de saude
**Carla, 37 anos, farmaceutica da atencao basica municipal**  
Carla atende alto volume de pacientes por dia, lida com prescricoes de multiplos profissionais e precisa identificar interacoes de risco sem atrasar o atendimento. Ela busca uma plataforma simples, confiavel e auditavel, que reduza retrabalho e melhore a qualidade do cuidado.

## Problema que o MVP resolve
- Historico de medicacao fragmentado entre sistemas e papel.
- Baixa visibilidade de risco de interacao medicamentosa.
- Dificuldade para acompanhar adesao do paciente no dia a dia.
- Falta de um fluxo digital unico entre prescricao, alerta clinico e visao do paciente.

## Proposta de valor
- Reduzir erros de medicacao com alertas de interacao.
- Melhorar continuidade do cuidado com dados de prescricao e adesao.
- Garantir seguranca e rastreabilidade com fluxo JWT ponta a ponta.
- Facilitar validacao tecnica com testes automatizados e ambiente reproduzivel.

## O que ja funciona no MVP (estado atual)
### Paciente
- Consultar o proprio cartao de medicacao em `/pacientes/me/cartao-medicacao`.
- Visualizar alertas de risco em `/pacientes/me/alertas`.
- Visualizar doses do dia em `/pacientes/me/doses-hoje`.
- Registrar dose realizada em `/pacientes/me/doses/{doseId}/registro`.
- Registrar comentario opcional de tomada (ate 300 caracteres).
- Consultar historico de tomadas em `/pacientes/me/doses/historico`.
- Consultar lembretes ativos em `/pacientes/me/lembretes`.
- Consultar disponibilidade de medicamento por UBS em `/pacientes/me/disponibilidade-ubs?medicamento=...`.
- Gerenciar alergias proprias em `/pacientes/me/alergias` (criar, editar, remover, listar).
- Visualizar tipo sanguineo em `/pacientes/me/perfil-clinico` (somente leitura para paciente).

### Profissional
- Criar prescricoes por paciente em `/pacientes/{pacienteId}/medicamentos`.
- Definir medicamento, dosagem, frequencia, horarios, orientacoes de uso, inicio e fim da receita.
- Disparar processamento de seguranca por evento para detectar interacoes.
- Em caso de interacao identificada, confirmar risco explicitamente com `confirmarRiscoInteracao=true` para prosseguir.
- Visualizar historico de tomadas por paciente em `/pacientes/{pacienteId}/doses/historico`.
- Gerenciar alergias do paciente em `/pacientes/{pacienteId}/alergias`.
- Definir tipo sanguineo do paciente em `/pacientes/{pacienteId}/tipo-sanguineo`.

### Motor clinico e integracao
- Deteccao de interacao medicamentosa no `safety-service`.
- Propagacao de alertas para `patient-service` via RabbitMQ.
- Agendamento de evento de receita perto do vencimento no `medication-service`.

## Aderencia ao produto desejado (gap transparente)
Este MVP cobre boa parte do fluxo principal, mas ainda nao implementa 100% da visao final descrita.

Ja coberto:
- Cartao de medicacao consolidado.
- Prescricao por profissional com data de vigencia.
- Campo de orientacoes de uso na prescricao.
- Cadastro e manutencao de alergias por paciente e profissional.
- Tipo sanguineo com escrita exclusiva por profissional e leitura pelo paciente.
- Alertas de interacao e visao do paciente.
- Decisao clinica bloqueante antes de confirmar prescricao com risco.
- Lembretes ativos via endpoint de lembretes do paciente.
- Disponibilidade por UBS com catalogo inicial no MVP.
- Registro de tomada com comentario opcional e historico de adesao.
- Rotina tecnica para receita perto de vencer (evento).

Ainda nao coberto nesta versao:
- Cadastro de medicacoes por conta propria pelo paciente.
- Campo de via de administracao na prescricao.
- Integracao de disponibilidade por UBS com estoque real municipal/estadual.
- Lembretes ativos por canal externo (push/sms/whatsapp/app) em producao.
- Login por CPF + data de nascimento + senha (hoje o login e por usuario + senha + tipo).
- Trilha de auditoria clinica completa para todas as acoes.
- Tela/app frontend para operacao assistida com pacientes reais.

## Roadmap curto de evolucao
- **R1**: adicionar `origemPrescricao` (profissional x autodeclarada) e trilha de auditoria na prescricao.
- **R2**: endpoint para paciente cadastrar medicacao autodeclarada.
- **R3**: integrar disponibilidade por UBS com fonte oficial de estoque.
- **R4**: notificacoes multicanal de lembrete (dose e receita proxima do vencimento).
- **R5**: registrar justificativa clinica obrigatoria ao confirmar risco de interacao.

## Arquitetura (MVP)
- `auth-service` (`8083`): autenticacao e emissao de JWT.
- `medication-service` (`8080`): gestao de prescricoes.
- `safety-service` (`8081`): regras e alertas de interacao medicamentosa.
- `patient-service` (`8082`): visao consolidada para paciente/profissional.
- `adherence-service` (`8084`): acompanhamento de adesao ao tratamento.
- `rabbitmq` (`15673`): mensageria para integracao assíncrona.

## Stack tecnica
- Java 21
- Spring Boot
- PostgreSQL
- RabbitMQ
- Docker / Docker Compose
- Maven

## Como executar localmente
### 1) Pre-requisitos
- Docker + Docker Compose
- Java 21
- Maven
- curl
- python3
- openssl

### 2) Subir ambiente
```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export SEED_PACIENTE_LOGIN_1="pac_$(date +%s)"
export SEED_PACIENTE_ID_1="1"
export SEED_PACIENTE_LOGIN_2="pac_$(date +%s)_2"
export SEED_PACIENTE_ID_2="2"
export AUTH_SEED_PACIENTE_LOGIN="$SEED_PACIENTE_LOGIN_1"
export AUTH_SEED_PACIENTE_SENHA="$(openssl rand -hex 8)"
export AUTH_SEED_PROF_LOGIN="prof_$(date +%s)"
export AUTH_SEED_PROF_SENHA="$(openssl rand -hex 8)"
docker compose -f infra/docker-compose.yml up -d --build
```

### 3) Validar fluxo E2E
```bash
AUTH_LOGIN="$AUTH_SEED_PROF_LOGIN" AUTH_SENHA="$AUTH_SEED_PROF_SENHA" AUTH_TIPO=PROFISSIONAL PACIENTE_ID="$SEED_PACIENTE_ID_1" ./test_e2e_local.sh
```

Se o fluxo estiver correto, o script finaliza com `E2E OK`.

## Validacao completa (one-shot)
O script `validate_all.sh` executa o ciclo completo:
- sobe o ambiente
- roda o E2E
- opcionalmente executa testes Maven
- derruba ambiente (ou mantem, se configurado)

```bash
./validate_all.sh
```

Variaveis suportadas:
- `RUN_MVN_TESTS=1` para executar testes Maven principais.
- `KEEP_UP=1` para manter containers no final.
- `JWT_SECRET=...` para informar segredo JWT explicitamente.
- `AUTH_SEED_PACIENTE_LOGIN`, `AUTH_SEED_PACIENTE_SENHA`, `AUTH_SEED_PROF_LOGIN`, `AUTH_SEED_PROF_SENHA` para controlar seed de usuarios no `auth-service`.
- `SEED_PACIENTE_LOGIN_1`, `SEED_PACIENTE_ID_1`, `SEED_PACIENTE_LOGIN_2`, `SEED_PACIENTE_ID_2` para mapeamento login/paciente nos servicos.

Exemplo:
```bash
RUN_MVN_TESTS=1 KEEP_UP=1 ./validate_all.sh
```

Observacao: se `JWT_SECRET` e seeds nao forem informadas, `validate_all.sh` gera valores automaticamente para a execucao.

## Testes por servico
```bash
mvn -B -f services/medication-service/pom.xml test
mvn -B -f services/patient-service/pom.xml test
mvn -B -f services/adherence-service/pom.xml test
```

## Documentacao de API (Swagger/OpenAPI)
Swagger UI:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`

OpenAPI JSON:
- `http://localhost:8080/v3/api-docs`
- `http://localhost:8081/v3/api-docs`
- `http://localhost:8082/v3/api-docs`
- `http://localhost:8083/v3/api-docs`
- `http://localhost:8084/v3/api-docs`

## Checklist final de entrega
- [ ] Ambiente sobe sem erro via Docker Compose.
- [ ] E2E retorna `E2E OK`.
- [ ] Endpoint protegido sem token retorna `401`.
- [ ] Endpoint protegido com token retorna `200` ou `201`.
- [ ] Swagger dos 5 servicos acessivel.
- [ ] Repositorio em estado consistente para push.

## CI
Pipeline em `.github/workflows/ci-jwt.yml`:
- Etapa 1: valida integracao JWT no `medication-service`.
- Etapa 2: executa stack Docker + E2E para validar fluxo ponta a ponta.
