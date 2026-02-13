package br.gov.farmasus.safety.service;

import br.gov.farmasus.safety.config.RabbitMessagingProperties;
import br.gov.farmasus.safety.domain.AlertaInteracao;
import br.gov.farmasus.safety.domain.InteracaoMedicamento;
import br.gov.farmasus.safety.domain.PrescricaoPaciente;
import br.gov.farmasus.safety.dto.AlertaInteracaoCriadoEvent;
import br.gov.farmasus.safety.dto.AlertaResponse;
import br.gov.farmasus.safety.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.safety.dto.VerificacaoInteracaoResponse;
import br.gov.farmasus.safety.repository.AlertaInteracaoRepository;
import br.gov.farmasus.safety.repository.PrescricaoPacienteRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  private final PrescricaoPacienteRepository prescricaoRepository;
  private final AlertaInteracaoRepository alertaRepository;
  private final InteracaoMedicamentoService interacaoService;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitMessagingProperties rabbitMessagingProperties;

  public AlertaService(
      PrescricaoPacienteRepository prescricaoRepository,
      AlertaInteracaoRepository alertaRepository,
      InteracaoMedicamentoService interacaoService,
      RabbitTemplate rabbitTemplate,
      RabbitMessagingProperties rabbitMessagingProperties) {
    this.prescricaoRepository = prescricaoRepository;
    this.alertaRepository = alertaRepository;
    this.interacaoService = interacaoService;
    this.rabbitTemplate = rabbitTemplate;
    this.rabbitMessagingProperties = rabbitMessagingProperties;
  }

  @Transactional
  public void processarPrescricao(PrescricaoCriadaEvent event) {
    if (alertaRepository.existsByPrescricaoId(event.prescricaoId())) {
      return;
    }
    PrescricaoPaciente nova = new PrescricaoPaciente();
    nova.setPrescricaoId(event.prescricaoId());
    nova.setPacienteId(event.pacienteId());
    nova.setNomeMedicamento(event.nomeMedicamento());
    nova.setInicio(event.inicio());
    nova.setFim(event.fim());
    prescricaoRepository.save(nova);

    LocalDate hoje = LocalDate.now(ZONA_BR);
    List<PrescricaoPaciente> prescricoesAtivas = prescricaoRepository
        .findAllByPacienteIdAndInicioLessThanEqualAndFimGreaterThanEqual(event.pacienteId(), hoje, hoje)
        .stream()
        .filter(prescricao -> !prescricao.getPrescricaoId().equals(event.prescricaoId()))
        .toList();

    for (PrescricaoPaciente prescricao : prescricoesAtivas) {
      Optional<InteracaoMedicamento> interacaoOpt = interacaoService.buscarInteracao(
          event.nomeMedicamento(),
          prescricao.getNomeMedicamento()
      );
      if (interacaoOpt.isEmpty()) {
        continue;
      }
      InteracaoMedicamento interacao = interacaoOpt.get();
      AlertaInteracao alerta = new AlertaInteracao();
      alerta.setEventId(event.eventId());
      alerta.setCorrelationId(event.correlationId());
      alerta.setCriadoEm(event.criadoEm() != null ? event.criadoEm() : OffsetDateTime.now(ZONA_BR));
      alerta.setPacienteId(event.pacienteId());
      alerta.setPrescricaoId(event.prescricaoId());
      alerta.setSeveridade(interacao.getSeveridade());
      alerta.setMensagem(interacao.getMensagem());
      alerta.setRecomendacao(interacao.getRecomendacao());

      AlertaInteracao salvo;
      try {
        salvo = alertaRepository.save(alerta);
      } catch (DataIntegrityViolationException e) {
        continue;
      }

      AlertaInteracaoCriadoEvent alertaEvent = new AlertaInteracaoCriadoEvent(
          salvo.getEventId(),
          salvo.getCorrelationId(),
          salvo.getCriadoEm(),
          salvo.getId(),
          salvo.getPacienteId(),
          salvo.getPrescricaoId(),
          salvo.getSeveridade(),
          salvo.getMensagem(),
          salvo.getRecomendacao()
      );

      rabbitTemplate.convertAndSend(
          rabbitMessagingProperties.exchange().events(),
          rabbitMessagingProperties.routing().safetyAlertCreated(),
          alertaEvent);
    }
  }

  @Transactional(readOnly = true)
  public List<AlertaResponse> listarAlertas(Long pacienteId) {
    return alertaRepository.findAllByPacienteIdOrderByCriadoEmDesc(pacienteId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private AlertaResponse toResponse(AlertaInteracao alerta) {
    return new AlertaResponse(
        alerta.getId(),
        alerta.getEventId(),
        alerta.getCorrelationId(),
        alerta.getCriadoEm(),
        alerta.getPacienteId(),
        alerta.getPrescricaoId(),
        alerta.getSeveridade(),
        alerta.getMensagem(),
        alerta.getRecomendacao()
    );
  }

  @Transactional(readOnly = true)
  public VerificacaoInteracaoResponse verificarInteracaoBloqueante(Long pacienteId, String nomeMedicamento) {
    LocalDate hoje = LocalDate.now(ZONA_BR);
    List<PrescricaoPaciente> prescricoesAtivas = prescricaoRepository
        .findAllByPacienteIdAndInicioLessThanEqualAndFimGreaterThanEqual(pacienteId, hoje, hoje);

    InteracaoMedicamento melhorInteracao = null;
    String medicamentoConflitante = null;
    int melhorRank = Integer.MAX_VALUE;

    for (PrescricaoPaciente prescricaoAtiva : prescricoesAtivas) {
      Optional<InteracaoMedicamento> interacaoOpt = interacaoService.buscarInteracao(
          nomeMedicamento,
          prescricaoAtiva.getNomeMedicamento()
      );
      if (interacaoOpt.isEmpty()) {
        continue;
      }

      InteracaoMedicamento interacao = interacaoOpt.get();
      int rank = rankSeveridade(interacao.getSeveridade());
      if (rank < melhorRank) {
        melhorRank = rank;
        melhorInteracao = interacao;
        medicamentoConflitante = prescricaoAtiva.getNomeMedicamento();
      }
    }

    if (melhorInteracao == null) {
      return new VerificacaoInteracaoResponse(false, null, null, null, null);
    }

    return new VerificacaoInteracaoResponse(
        true,
        melhorInteracao.getSeveridade(),
        melhorInteracao.getMensagem(),
        melhorInteracao.getRecomendacao(),
        medicamentoConflitante
    );
  }

  private int rankSeveridade(br.gov.farmasus.safety.domain.Severidade severidade) {
    return switch (severidade) {
      case CONTRAINDICADA -> 0;
      case ALTA -> 1;
      case MODERADA -> 2;
      case BAIXA -> 3;
    };
  }
}
