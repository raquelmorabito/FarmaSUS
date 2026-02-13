package br.gov.farmasus.medication.service;

import br.gov.farmasus.medication.config.RabbitConfig;
import br.gov.farmasus.medication.domain.Prescricao;
import br.gov.farmasus.medication.dto.ReceitaPertoVencerEvent;
import br.gov.farmasus.medication.repository.PrescricaoRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReceitaPertoVencerScheduler {
  private static final String ROUTING_KEY = "medication.prescription.expiring";
  private static final ZoneOffset OFFSET_BR = ZoneOffset.ofHours(-3);
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  private final PrescricaoRepository prescricaoRepository;
  private final RabbitTemplate rabbitTemplate;
  private final int diasAntecedencia;

  public ReceitaPertoVencerScheduler(
      PrescricaoRepository prescricaoRepository,
      RabbitTemplate rabbitTemplate,
      @Value("${jobs.receita-perto-vencer.dias-antecedencia:7}") int diasAntecedencia) {
    this.prescricaoRepository = prescricaoRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.diasAntecedencia = diasAntecedencia;
  }

  @Scheduled(
      cron = "${jobs.receita-perto-vencer.cron:0 0 8 * * *}",
      zone = "${jobs.receita-perto-vencer.zone:America/Sao_Paulo}")
  @Transactional(readOnly = true)
  public void processarReceitasPertoDeVencer() {
    LocalDate hoje = LocalDate.now(ZONA_BR);
    LocalDate dataAlvo = hoje.plusDays(diasAntecedencia);

    List<Prescricao> prescricoes = prescricaoRepository.findAllByFim(dataAlvo);
    for (Prescricao prescricao : prescricoes) {
      ReceitaPertoVencerEvent evento = new ReceitaPertoVencerEvent(
          UUID.randomUUID(),
          UUID.randomUUID(),
          OffsetDateTime.now(OFFSET_BR),
          prescricao.getPacienteId(),
          prescricao.getId(),
          prescricao.getNomeMedicamento(),
          prescricao.getFim(),
          diasAntecedencia
      );
      rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_EVENTS, ROUTING_KEY, evento);
    }
  }
}
