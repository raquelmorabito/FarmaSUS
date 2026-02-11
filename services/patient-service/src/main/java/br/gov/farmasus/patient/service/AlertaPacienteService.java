package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.domain.AlertaPaciente;
import br.gov.farmasus.patient.dto.AlertaInteracaoCriadoEvent;
import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.repository.AlertaPacienteRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaPacienteService {
  private final AlertaPacienteRepository alertaRepository;

  public AlertaPacienteService(AlertaPacienteRepository alertaRepository) {
    this.alertaRepository = alertaRepository;
  }

  @Transactional
  public void registrarAlerta(AlertaInteracaoCriadoEvent event) {
    if (alertaRepository.existsByEventId(event.eventId())) {
      return;
    }
    AlertaPaciente alerta = new AlertaPaciente();
    alerta.setEventId(event.eventId());
    alerta.setCorrelationId(event.correlationId());
    alerta.setCriadoEm(event.criadoEm());
    alerta.setAlertaId(event.alertaId());
    alerta.setPacienteId(event.pacienteId());
    alerta.setPrescricaoId(event.prescricaoId());
    alerta.setSeveridade(event.severidade());
    alerta.setMensagem(event.mensagem());
    alerta.setRecomendacao(event.recomendacao());

    try {
      alertaRepository.save(alerta);
    } catch (DataIntegrityViolationException e) {
      return;
    }
  }

  @Transactional(readOnly = true)
  public List<AlertaPacienteResponse> listarAlertas(Long pacienteId) {
    return alertaRepository.findAllByPacienteIdOrderByCriadoEmDesc(pacienteId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private AlertaPacienteResponse toResponse(AlertaPaciente alerta) {
    return new AlertaPacienteResponse(
        alerta.getId(),
        alerta.getEventId(),
        alerta.getCorrelationId(),
        alerta.getCriadoEm(),
        alerta.getAlertaId(),
        alerta.getPacienteId(),
        alerta.getPrescricaoId(),
        alerta.getSeveridade(),
        alerta.getMensagem(),
        alerta.getRecomendacao()
    );
  }
}
