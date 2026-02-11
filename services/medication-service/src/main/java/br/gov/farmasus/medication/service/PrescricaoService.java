package br.gov.farmasus.medication.service;

import br.gov.farmasus.medication.config.RabbitConfig;
import br.gov.farmasus.medication.domain.Prescricao;
import br.gov.farmasus.medication.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.medication.dto.PrescricaoRequest;
import br.gov.farmasus.medication.dto.PrescricaoResponse;
import br.gov.farmasus.medication.repository.PrescricaoRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescricaoService {
  private static final String ROUTING_KEY = "medication.created";
  private static final ZoneOffset OFFSET_BR = ZoneOffset.ofHours(-3);

  private final PrescricaoRepository prescricaoRepository;
  private final RabbitTemplate rabbitTemplate;

  public PrescricaoService(PrescricaoRepository prescricaoRepository, RabbitTemplate rabbitTemplate) {
    this.prescricaoRepository = prescricaoRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Transactional
  public PrescricaoResponse criarPrescricao(Long pacienteId, PrescricaoRequest request) {
    Prescricao prescricao = new Prescricao();
    prescricao.setPacienteId(pacienteId);
    prescricao.setNomeMedicamento(request.nomeMedicamento());
    prescricao.setDosagem(request.dosagem());
    prescricao.setFrequenciaDiaria(request.frequenciaDiaria());
    prescricao.setHorarios(request.horarios());
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
        prescricao.getInicio(),
        prescricao.getFim()
    );
  }
}
