package br.gov.farmasus.safety.service;

import br.gov.farmasus.safety.config.RabbitConfig;
import br.gov.farmasus.safety.domain.AlertaInteracao;
import br.gov.farmasus.safety.domain.InteracaoMedicamento;
import br.gov.farmasus.safety.domain.PrescricaoPaciente;
import br.gov.farmasus.safety.dto.AlertaInteracaoCriadoEvent;
import br.gov.farmasus.safety.dto.AlertaResponse;
import br.gov.farmasus.safety.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.safety.repository.AlertaInteracaoRepository;
import br.gov.farmasus.safety.repository.PrescricaoPacienteRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {
  private static final String ALERTA_ROUTING_KEY = "safety.alert.created";
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  private final PrescricaoPacienteRepository prescricaoRepository;
  private final AlertaInteracaoRepository alertaRepository;
  private final InteracaoMedicamentoService interacaoService;
  private final RabbitTemplate rabbitTemplate;

  public AlertaService(
      PrescricaoPacienteRepository prescricaoRepository,
      AlertaInteracaoRepository alertaRepository,
      InteracaoMedicamentoService interacaoService,
      RabbitTemplate rabbitTemplate) {
    this.prescricaoRepository = prescricaoRepository;
    this.alertaRepository = alertaRepository;
    this.interacaoService = interacaoService;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Transactional
  public void processarPrescricao(PrescricaoCriadaEvent event) {
    if (alertaRepository.existsByEventId(event.eventId())) {
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
      alerta.setCriadoEm(OffsetDateTime.now(ZONA_BR));
      alerta.setPacienteId(event.pacienteId());
      alerta.setPrescricaoId(event.prescricaoId());
      alerta.setSeveridade(interacao.getSeveridade());
      alerta.setMensagem(interacao.getMensagem());
      alerta.setRecomendacao(interacao.getRecomendacao());

      AlertaInteracao salvo;
      try {
        salvo = alertaRepository.save(alerta);
      } catch (DataIntegrityViolationException e) {
        return;
      }

      AlertaInteracaoCriadoEvent alertaEvent = new AlertaInteracaoCriadoEvent(
          UUID.randomUUID(),
          event.correlationId(),
          salvo.getCriadoEm(),
          salvo.getId(),
          salvo.getPacienteId(),
          salvo.getPrescricaoId(),
          salvo.getSeveridade(),
          salvo.getMensagem(),
          salvo.getRecomendacao()
      );

      rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, ALERTA_ROUTING_KEY, alertaEvent);
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
}
