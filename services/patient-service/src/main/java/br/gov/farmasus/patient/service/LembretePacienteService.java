package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.dto.LembretePacienteResponse;
import br.gov.farmasus.patient.dto.PrescricaoPacienteResponse;
import br.gov.farmasus.patient.dto.DoseHojeResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
public class LembretePacienteService {
  private final RestTemplate restTemplate;
  private final String medicationServiceUrl;
  private final String adherenceServiceUrl;

  public LembretePacienteService(
      @Value("${services.medication.url:http://localhost:8080}") String medicationServiceUrl,
      @Value("${services.adherence.url:http://localhost:8084}") String adherenceServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.medicationServiceUrl = medicationServiceUrl;
    this.adherenceServiceUrl = adherenceServiceUrl;
  }

  public List<LembretePacienteResponse> listarLembretes(Long pacienteId, String authorizationHeader) {
    List<PrescricaoPacienteResponse> prescricoes = buscarPrescricoes(pacienteId, authorizationHeader);
    List<LembretePacienteResponse> lembretes = new ArrayList<>();

    LocalDate hoje = LocalDate.now();
    LocalDate limite = hoje.plusDays(7);

    for (PrescricaoPacienteResponse prescricao : prescricoes) {
      if (!prescricao.fim().isBefore(hoje) && !prescricao.fim().isAfter(limite)) {
        lembretes.add(new LembretePacienteResponse(
            "RECEITA_VENCIMENTO",
            "Receita proxima do vencimento",
            "A receita de " + prescricao.nomeMedicamento() + " vence em " + prescricao.fim() + ". Procure sua UBS.",
            prescricao.fim(),
            null,
            "ALTA"
        ));
      }
    }

    List<DoseHojeResponse> dosesHoje = buscarDosesHoje(authorizationHeader);
    for (DoseHojeResponse dose : dosesHoje) {
      if (!"PENDENTE".equalsIgnoreCase(dose.status())) {
        continue;
      }
      lembretes.add(new LembretePacienteResponse(
          "DOSE_HOJE",
          "Horario de medicacao",
          "Tomar " + dose.nomeMedicamento() + " as " + dose.horario() + ".",
          dose.data(),
          dose.horario(),
          "MEDIA"
      ));
    }

    lembretes.sort(Comparator
        .comparing(LembretePacienteResponse::data)
        .thenComparing(l -> l.horario() == null ? java.time.LocalTime.MAX : l.horario()));
    return lembretes;
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
      throw new ResponseStatusException(BAD_GATEWAY, "Falha ao buscar prescricoes para lembretes");
    }
  }

  private List<DoseHojeResponse> buscarDosesHoje(String authorizationHeader) {
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
