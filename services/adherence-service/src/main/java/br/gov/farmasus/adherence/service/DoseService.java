package br.gov.farmasus.adherence.service;

import br.gov.farmasus.adherence.config.RabbitConfig;
import br.gov.farmasus.adherence.domain.Dose;
import br.gov.farmasus.adherence.domain.RegistroDose;
import br.gov.farmasus.adherence.dto.DoseRegistradaEvent;
import br.gov.farmasus.adherence.dto.DoseResponse;
import br.gov.farmasus.adherence.dto.RegistroDoseRequest;
import br.gov.farmasus.adherence.repository.DoseRepository;
import br.gov.farmasus.adherence.repository.RegistroDoseRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DoseService {
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  private final DoseRepository doseRepository;
  private final RegistroDoseRepository registroDoseRepository;
  private final RabbitTemplate rabbitTemplate;

  public DoseService(DoseRepository doseRepository,
                     RegistroDoseRepository registroDoseRepository,
                     RabbitTemplate rabbitTemplate) {
    this.doseRepository = doseRepository;
    this.registroDoseRepository = registroDoseRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Transactional(readOnly = true)
  public List<DoseResponse> listarDosesHoje(Long pacienteId) {
    LocalDate hoje = LocalDate.now(ZONA_BR);
    return doseRepository.findAllByPacienteIdAndDataOrderByHorario(pacienteId, hoje)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public DoseResponse registrarDose(Long pacienteId, Long doseId, RegistroDoseRequest request) {
    Dose dose = doseRepository.findByIdAndPacienteId(doseId, pacienteId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dose nao encontrada"));

    dose.setStatus(request.status());
    doseRepository.save(dose);

    RegistroDose registro = new RegistroDose();
    registro.setDose(dose);
    registro.setStatus(request.status());
    registro.setRegistradoEm(request.registradoEm());
    registroDoseRepository.save(registro);

    publicarEventoRegistro(dose, request);

    return toResponse(dose);
  }

  private DoseResponse toResponse(Dose dose) {
    return new DoseResponse(
        dose.getId(),
        dose.getPrescricaoId(),
        dose.getNomeMedicamento(),
        dose.getData(),
        dose.getHorario(),
        dose.getStatus().name()
    );
  }

  private void publicarEventoRegistro(Dose dose, RegistroDoseRequest request) {
    DoseRegistradaEvent event = new DoseRegistradaEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        dose.getId(),
        dose.getPrescricaoId(),
        dose.getPacienteId(),
        request.status().name(),
        request.registradoEm()
    );
    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_EVENTS, RabbitConfig.DOSE_REGISTRADA_ROUTING_KEY, event);
  }
}
