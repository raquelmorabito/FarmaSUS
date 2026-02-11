package br.gov.farmasus.safety.controller;

import br.gov.farmasus.safety.dto.AlertaResponse;
import br.gov.farmasus.safety.service.AlertaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alertas/paciente")
public class AlertaController {
  private final AlertaService alertaService;

  public AlertaController(AlertaService alertaService) {
    this.alertaService = alertaService;
  }

  @GetMapping("/{pacienteId}")
  public ResponseEntity<List<AlertaResponse>> listarAlertas(@PathVariable Long pacienteId) {
    return ResponseEntity.ok(alertaService.listarAlertas(pacienteId));
  }
}
