package br.gov.farmasus.adherence.controller;

import br.gov.farmasus.adherence.dto.DoseResponse;
import br.gov.farmasus.adherence.dto.RegistroDoseHistoricoResponse;
import br.gov.farmasus.adherence.dto.RegistroDoseRequest;
import br.gov.farmasus.adherence.model.TipoUsuario;
import br.gov.farmasus.adherence.model.UsuarioPrincipal;
import br.gov.farmasus.adherence.service.DoseService;
import br.gov.farmasus.adherence.service.PacienteLoginMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/{pacienteId}")
public class DoseController {
  private final DoseService doseService;
  private final PacienteLoginMapper pacienteLoginMapper;

  public DoseController(DoseService doseService, PacienteLoginMapper pacienteLoginMapper) {
    this.doseService = doseService;
    this.pacienteLoginMapper = pacienteLoginMapper;
  }

  @GetMapping("/doses-hoje")
  public ResponseEntity<List<DoseResponse>> listarDosesHoje(@PathVariable Long pacienteId) {
    validarAcessoPaciente(pacienteId);
    return ResponseEntity.ok(doseService.listarDosesHoje(pacienteId));
  }

  @PostMapping("/doses/{doseId}/registro")
  public ResponseEntity<DoseResponse> registrarDose(
      @PathVariable Long pacienteId,
      @PathVariable Long doseId,
      @Valid @RequestBody RegistroDoseRequest request) {
    validarAcessoPaciente(pacienteId);
    return ResponseEntity.ok(doseService.registrarDose(pacienteId, doseId, request));
  }

  @GetMapping("/doses/historico")
  public ResponseEntity<List<RegistroDoseHistoricoResponse>> listarHistorico(@PathVariable Long pacienteId) {
    validarAcessoPaciente(pacienteId);
    return ResponseEntity.ok(doseService.listarHistoricoTomadas(pacienteId));
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
