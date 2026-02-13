package br.gov.farmasus.medication.service;

import br.gov.farmasus.medication.dto.VerificacaoInteracaoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SafetyServiceClient {
  private final RestTemplate restTemplate;
  private final String safetyServiceUrl;

  public SafetyServiceClient(
      RestTemplate restTemplate,
      @Value("${services.safety.url}") String safetyServiceUrl) {
    this.restTemplate = restTemplate;
    this.safetyServiceUrl = safetyServiceUrl;
  }

  @Retry(name = "safetyService")
  @CircuitBreaker(name = "safetyService")
  public VerificacaoInteracaoResponse verificarInteracao(Long pacienteId, String medicamento) {
    String url = UriComponentsBuilder
        .fromHttpUrl(safetyServiceUrl + "/alertas/paciente/verificacao-interacao")
        .queryParam("pacienteId", pacienteId)
        .queryParam("medicamento", medicamento)
        .toUriString();
    try {
      ResponseEntity<VerificacaoInteracaoResponse> response = restTemplate.getForEntity(
          url,
          VerificacaoInteracaoResponse.class
      );
      return response.getBody();
    } catch (RestClientException ex) {
      throw unavailable();
    }
  }

  private static ResponseStatusException unavailable() {
    return new ResponseStatusException(
        HttpStatus.BAD_GATEWAY,
        "Falha ao validar interacao medicamentosa no safety-service"
    );
  }
}
