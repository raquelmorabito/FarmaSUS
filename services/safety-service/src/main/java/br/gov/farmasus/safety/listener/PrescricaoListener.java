package br.gov.farmasus.safety.listener;

import br.gov.farmasus.safety.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.safety.service.AlertaService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PrescricaoListener {
  private final AlertaService alertaService;

  public PrescricaoListener(AlertaService alertaService) {
    this.alertaService = alertaService;
  }

  @RabbitListener(queues = "${rabbitmq.messaging.queue.medication-created}")
  public void consumirPrescricaoCriada(PrescricaoCriadaEvent event) {
    alertaService.processarPrescricao(event);
  }
}
