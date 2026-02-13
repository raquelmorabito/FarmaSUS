## FarmaSUS JWT Validation

### E2E script version
- `test_e2e_local.sh` version: `1.0.0`

### Run E2E locally
```bash
AUTH_LOGIN=prof1 AUTH_SENHA=senha123 AUTH_TIPO=PROFISSIONAL ./test_e2e_local.sh
```

### Automated JWT integration test
```bash
mvn -B -f services/medication-service/pom.xml -Dtest=JwtFlowIntegrationTest test
```

### CI
- Workflow: `.github/workflows/ci-jwt.yml`
- Stage 1: runs `JwtFlowIntegrationTest` in `medication-service`
- Stage 2: runs Docker Compose + `test_e2e_local.sh` and validates end-to-end JWT flow
