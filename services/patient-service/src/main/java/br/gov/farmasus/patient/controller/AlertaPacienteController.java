package br.gov.farmasus.patient.controller;

import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.model.TipoUsuario;
import br.gov.farmasus.patient.model.UsuarioPrincipal;
import br.gov.farmasus.patient.service.AlertaPacienteService;
import br.gov.farmasus.patient.service.PacienteLoginMapper;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/{pacienteId}/alertas")
public class AlertaPacienteController {
  private final AlertaPacienteService alertaService;
  private final PacienteLoginMapper pacienteLoginMapper;

  public AlertaPacienteController(AlertaPacienteService alertaService, PacienteLoginMapper pacienteLoginMapper) {
    this.alertaService = alertaService;
    this.pacienteLoginMapper = pacienteLoginMapper;
  }

  @GetMapping
  public ResponseEntity<List<AlertaPacienteResponse>> listarAlertas(@PathVariable Long pacienteId) {
    validarAcessoPaciente(pacienteId);
    return ResponseEntity.ok(alertaService.listarAlertas(pacienteId));
  }

  private void validarAcessoPaciente(Long pacienteId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
      throw new AuthenticationCredentialsNotFoundException("Nao autenticado.");
    }

    TipoUsuario tipoUsuario;
    try {
      tipoUsuario = TipoUsuario.valueOf(principal.tipoUsuario());
    } catch (IllegalArgumentException ex) {
      throw new BadCredentialsException("Token invalido.");
    }
    if (tipoUsuario == TipoUsuario.PACIENTE) {
      Long pacienteAutorizado = pacienteLoginMapper.obterPacienteId(principal.login())
          .orElseThrow(() -> new AccessDeniedException("Acesso negado ao paciente informado."));
      if (!pacienteAutorizado.equals(pacienteId)) {
        throw new AccessDeniedException("Acesso negado ao paciente informado.");
      }
    }
  }
}
