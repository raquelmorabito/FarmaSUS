package br.gov.farmasus.patient.listener;

import br.gov.farmasus.patient.config.RabbitConfig;
import br.gov.farmasus.patient.dto.AlertaInteracaoCriadoEvent;
import br.gov.farmasus.patient.service.AlertaPacienteService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertaPacienteListener {
  private final AlertaPacienteService alertaService;

  public AlertaPacienteListener(AlertaPacienteService alertaService) {
    this.alertaService = alertaService;
  }

  @RabbitListener(queues = RabbitConfig.ALERTA_CRIADO_QUEUE)
  public void consumirAlertaCriado(AlertaInteracaoCriadoEvent event) {
    alertaService.registrarAlerta(event);
  }
}
