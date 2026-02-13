package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.dto.DoseHojeResponse;
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
public class AdherenceServiceClient {
  private final RestTemplate restTemplate;
  private final String adherenceServiceUrl;

  public AdherenceServiceClient(
      RestTemplate restTemplate,
      @Value("${services.adherence.url}") String adherenceServiceUrl) {
    this.restTemplate = restTemplate;
    this.adherenceServiceUrl = adherenceServiceUrl;
  }

  @Retry(name = "adherenceService")
  @CircuitBreaker(name = "adherenceService")
  public List<DoseHojeResponse> buscarDosesHoje(String authorizationHeader) {
    String url = adherenceServiceUrl + "/pacientes/me/doses-hoje";
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<List<DoseHojeResponse>> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          new ParameterizedTypeReference<>() {}
      );
      List<DoseHojeResponse> body = response.getBody();
      return body == null ? List.of() : body;
    } catch (RestClientException ex) {
      throw new ResponseStatusException(BAD_GATEWAY, "Falha ao buscar doses para lembretes");
    }
  }
}
