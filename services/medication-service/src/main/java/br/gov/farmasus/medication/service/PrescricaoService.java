package br.gov.farmasus.medication.service;

import br.gov.farmasus.medication.config.RabbitMessagingProperties;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PrescricaoService {
  private static final ZoneOffset OFFSET_BR = ZoneOffset.ofHours(-3);

  private final PrescricaoRepository prescricaoRepository;
  private final RabbitTemplate rabbitTemplate;
  private final SafetyServiceClient safetyServiceClient;
  private final RabbitMessagingProperties rabbitMessagingProperties;

  public PrescricaoService(
      PrescricaoRepository prescricaoRepository,
      RabbitTemplate rabbitTemplate,
      RabbitMessagingProperties rabbitMessagingProperties,
      SafetyServiceClient safetyServiceClient) {
    this.prescricaoRepository = prescricaoRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.rabbitMessagingProperties = rabbitMessagingProperties;
    this.safetyServiceClient = safetyServiceClient;
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

    rabbitTemplate.convertAndSend(
        rabbitMessagingProperties.exchange().events(),
        rabbitMessagingProperties.routing().medicationCreated(),
        evento);

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
    VerificacaoInteracaoResponse verificacao =
        safetyServiceClient.verificarInteracao(pacienteId, request.nomeMedicamento());

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
