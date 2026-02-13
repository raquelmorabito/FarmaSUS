package br.gov.farmasus.medication.service;

import br.gov.farmasus.medication.config.RabbitConfig;
import br.gov.farmasus.medication.domain.Prescricao;
import br.gov.farmasus.medication.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.medication.dto.PrescricaoRequest;
import br.gov.farmasus.medication.dto.PrescricaoResponse;
import br.gov.farmasus.medication.dto.VerificacaoInteracaoResponse;
import br.gov.farmasus.medication.repository.PrescricaoRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PrescricaoService {
  private static final String ROUTING_KEY = "medication.created";
  private static final ZoneOffset OFFSET_BR = ZoneOffset.ofHours(-3);

  private final PrescricaoRepository prescricaoRepository;
  private final RabbitTemplate rabbitTemplate;
  private final RestTemplate restTemplate;
  private final String safetyServiceUrl;

  public PrescricaoService(
      PrescricaoRepository prescricaoRepository,
      RabbitTemplate rabbitTemplate,
      @Value("${services.safety.url:http://localhost:8081}") String safetyServiceUrl) {
    this.prescricaoRepository = prescricaoRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.restTemplate = new RestTemplate();
    this.safetyServiceUrl = safetyServiceUrl;
  }

  @Transactional
  public PrescricaoResponse criarPrescricao(Long pacienteId, PrescricaoRequest request) {
    validarInteracaoBloqueante(pacienteId, request);

    Prescricao prescricao = new Prescricao();
    prescricao.setPacienteId(pacienteId);
    prescricao.setNomeMedicamento(request.nomeMedicamento());
    prescricao.setDosagem(request.dosagem());
    prescricao.setFrequenciaDiaria(request.frequenciaDiaria());
    prescricao.setHorarios(request.horarios());
    prescricao.setOrientacoesUso(request.orientacoesUso());
    prescricao.setInicio(request.inicio());
    prescricao.setFim(request.fim());

    Prescricao salva = prescricaoRepository.save(prescricao);

    PrescricaoCriadaEvent evento = new PrescricaoCriadaEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        OffsetDateTime.now(OFFSET_BR),
        salva.getPacienteId(),
        salva.getId(),
        salva.getNomeMedicamento(),
        salva.getDosagem(),
        salva.getFrequenciaDiaria(),
        salva.getHorarios(),
        salva.getInicio(),
        salva.getFim()
    );

    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_EVENTS, ROUTING_KEY, evento);

    return toResponse(salva);
  }

  @Transactional(readOnly = true)
  public List<PrescricaoResponse> listarPrescricoes(Long pacienteId) {
    return prescricaoRepository.findAllByPacienteId(pacienteId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private PrescricaoResponse toResponse(Prescricao prescricao) {
    return new PrescricaoResponse(
        prescricao.getId(),
        prescricao.getPacienteId(),
        prescricao.getNomeMedicamento(),
        prescricao.getDosagem(),
        prescricao.getFrequenciaDiaria(),
        prescricao.getHorarios(),
        prescricao.getOrientacoesUso(),
        prescricao.getInicio(),
        prescricao.getFim()
    );
  }

  private void validarInteracaoBloqueante(Long pacienteId, PrescricaoRequest request) {
    String url = UriComponentsBuilder
        .fromHttpUrl(safetyServiceUrl + "/alertas/paciente/verificacao-interacao")
        .queryParam("pacienteId", pacienteId)
        .queryParam("medicamento", request.nomeMedicamento())
        .toUriString();

    VerificacaoInteracaoResponse verificacao;
    try {
      ResponseEntity<VerificacaoInteracaoResponse> response = restTemplate.getForEntity(
          url,
          VerificacaoInteracaoResponse.class
      );
      verificacao = response.getBody();
    } catch (RestClientException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Falha ao validar interacao medicamentosa no safety-service"
      );
    }

    if (verificacao == null || !verificacao.possuiRisco()) {
      return;
    }

    boolean confirmou = Boolean.TRUE.equals(request.confirmarRiscoInteracao());
    if (confirmou) {
      return;
    }

    String detalhe = String.format(
        "Interacao detectada com '%s' (severidade: %s). %s",
        verificacao.medicamentoConflitante(),
        verificacao.severidade(),
        verificacao.recomendacao() == null ? "Confirme o risco para prosseguir." : verificacao.recomendacao()
    );
    throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        detalhe + " Envie confirmarRiscoInteracao=true para continuar."
    );
  }
}
