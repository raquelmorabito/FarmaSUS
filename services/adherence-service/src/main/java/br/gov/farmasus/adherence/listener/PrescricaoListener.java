package br.gov.farmasus.adherence.listener;

import br.gov.farmasus.adherence.config.RabbitConfig;
import br.gov.farmasus.adherence.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.adherence.service.PrescricaoAdesaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PrescricaoListener {
  private static final Logger logger = LoggerFactory.getLogger(PrescricaoListener.class);

  private final PrescricaoAdesaoService prescricaoAdesaoService;

  public PrescricaoListener(PrescricaoAdesaoService prescricaoAdesaoService) {
    this.prescricaoAdesaoService = prescricaoAdesaoService;
  }

  @RabbitListener(queues = RabbitConfig.PRESCRICAO_CRIADA_QUEUE)
  public void consumirPrescricaoCriada(PrescricaoCriadaEvent event) {
    if (event == null) {
      logger.info(
          "Evento de prescrição recebido: eventId=null, correlationId=null, pacienteId=null, prescricaoId=null, medicamento=null, inicio=null, fim=null, horarios=null");
      prescricaoAdesaoService.processarPrescricao(event);
      return;
    }

    logger.info(
        "Evento de prescrição recebido: eventId={}, correlationId={}, pacienteId={}, prescricaoId={}, medicamento={}, inicio={}, fim={}, horarios={}",
        event.eventId(),
        event.correlationId(),
        event.pacienteId(),
        event.prescricaoId(),
        event.nomeMedicamento(),
        event.inicio(),
        event.fim(),
        event.horarios());
    prescricaoAdesaoService.processarPrescricao(event);
  }
}
