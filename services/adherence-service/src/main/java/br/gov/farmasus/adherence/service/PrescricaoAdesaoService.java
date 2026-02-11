package br.gov.farmasus.adherence.service;

import br.gov.farmasus.adherence.domain.Dose;
import br.gov.farmasus.adherence.domain.DoseStatus;
import br.gov.farmasus.adherence.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.adherence.repository.DoseRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class PrescricaoAdesaoService {
  private static final DateTimeFormatter FORMATO_HORARIO = DateTimeFormatter.ofPattern("HH:mm");
  private static final Logger logger = LoggerFactory.getLogger(PrescricaoAdesaoService.class);
  private static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");

  private final DoseRepository doseRepository;

  public PrescricaoAdesaoService(DoseRepository doseRepository) {
    this.doseRepository = doseRepository;
  }

  public void processarPrescricao(PrescricaoCriadaEvent event) {
    if (event == null || event.prescricaoId() == null) {
      return;
    }

    if (doseRepository.existsByPrescricaoId(event.prescricaoId())) {
      return;
    }

    List<LocalTime> horarios = event.horarios() == null
        ? List.of()
        : event.horarios().stream().map(this::parseHorario).toList();

    List<Dose> doses = new ArrayList<>();
    LocalDate data = event.inicio();
    while (data != null && event.fim() != null && !data.isAfter(event.fim())) {
      for (LocalTime horario : horarios) {
        Dose dose = new Dose();
        dose.setPrescricaoId(event.prescricaoId());
        dose.setPacienteId(event.pacienteId());
        dose.setNomeMedicamento(event.nomeMedicamento());
        dose.setData(data);
        dose.setHorario(horario);
        dose.setStatus(DoseStatus.PENDENTE);
        doses.add(dose);
      }
      data = data.plusDays(1);
    }

    logger.info(
        "Doses geradas para pacienteId={} prescricaoId={} total={} dataBase={} (America/Sao_Paulo)",
        event.pacienteId(),
        event.prescricaoId(),
        doses.size(),
        LocalDate.now(ZONE_SAO_PAULO));

    if (!doses.isEmpty()) {
      salvarDosesIgnorandoDuplicadas(event, doses);
    }
  }

  private LocalTime parseHorario(String horario) {
    return LocalTime.parse(horario, FORMATO_HORARIO);
  }

  private void salvarDosesIgnorandoDuplicadas(PrescricaoCriadaEvent event, List<Dose> doses) {
    try {
      doseRepository.saveAllAndFlush(doses);
      return;
    } catch (DataIntegrityViolationException ex) {
      logger.warn(
          "Duplicidade ao salvar doses para pacienteId={} prescricaoId={}, seguindo sem reprocessar.",
          event.pacienteId(),
          event.prescricaoId());
    }

    for (Dose dose : doses) {
      try {
        doseRepository.saveAndFlush(dose);
      } catch (DataIntegrityViolationException ex) {
        continue;
      }
    }
  }
}
