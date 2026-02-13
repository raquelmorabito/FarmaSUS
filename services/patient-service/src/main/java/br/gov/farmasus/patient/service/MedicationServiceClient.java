package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.dto.PrescricaoPacienteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Component
public class MedicationServiceClient {
  private final RestTemplate restTemplate;
  private final String medicationServiceUrl;

  public MedicationServiceClient(
      RestTemplate restTemplate,
      @Value("${services.medication.url}") String medicationServiceUrl) {
    this.restTemplate = restTemplate;
    this.medicationServiceUrl = medicationServiceUrl;
  }

  @Retry(name = "medicationService")
  @CircuitBreaker(name = "medicationService")
  public List<PrescricaoPacienteResponse> buscarPrescricoes(Long pacienteId, String authorizationHeader) {
    String url = medicationServiceUrl + "/pacientes/" + pacienteId + "/medicamentos";
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    try {
      ResponseEntity<List<PrescricaoPacienteResponse>> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          new ParameterizedTypeReference<>() {}
      );
      List<PrescricaoPacienteResponse> body = response.getBody();
      return body == null ? List.of() : body;
    } catch (RestClientException ex) {
      throw new ResponseStatusException(BAD_GATEWAY, "Falha ao buscar prescricoes no medication-service");
    }
  }
}
