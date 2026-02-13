package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.domain.Severidade;
import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.dto.CartaoMedicacaoResponse;
import br.gov.farmasus.patient.dto.PrescricaoPacienteResponse;
import br.gov.farmasus.patient.dto.ResumoCartaoMedicacaoResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class CartaoMedicacaoService {
  private final AlertaPacienteService alertaPacienteService;
  private final RestTemplate restTemplate;
  private final String medicationServiceUrl;

  public CartaoMedicacaoService(
      AlertaPacienteService alertaPacienteService,
      @Value("${services.medication.url:http://localhost:8080}") String medicationServiceUrl) {
    this.alertaPacienteService = alertaPacienteService;
    this.restTemplate = new RestTemplate();
    this.medicationServiceUrl = medicationServiceUrl;
  }

  public CartaoMedicacaoResponse obterCartao(Long pacienteId, String authorizationHeader) {
    List<PrescricaoPacienteResponse> prescricoes = buscarPrescricoes(pacienteId, authorizationHeader);
    List<AlertaPacienteResponse> alertas = alertaPacienteService.listarAlertas(pacienteId);
    ResumoCartaoMedicacaoResponse resumo = montarResumo(prescricoes, alertas);

    return new CartaoMedicacaoResponse(pacienteId, prescricoes, alertas, resumo);
  }

  private List<PrescricaoPacienteResponse> buscarPrescricoes(Long pacienteId, String authorizationHeader) {
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

  private ResumoCartaoMedicacaoResponse montarResumo(
      List<PrescricaoPacienteResponse> prescricoes,
      List<AlertaPacienteResponse> alertas) {
    LocalDate hoje = LocalDate.now();
    int ativosHoje = (int) prescricoes.stream()
        .filter(p -> !p.inicio().isAfter(hoje) && !p.fim().isBefore(hoje))
        .count();

    int alertasCriticos = (int) alertas.stream()
        .filter(a -> a.severidade() == Severidade.CONTRAINDICADA || a.severidade() == Severidade.ALTA)
        .count();

    return new ResumoCartaoMedicacaoResponse(
        prescricoes.size(),
        alertas.size(),
        alertasCriticos,
        ativosHoje
    );
  }
}
