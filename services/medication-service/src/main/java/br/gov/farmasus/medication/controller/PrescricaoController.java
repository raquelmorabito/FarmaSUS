package br.gov.farmasus.medication.controller;

import br.gov.farmasus.medication.dto.PrescricaoRequest;
import br.gov.farmasus.medication.dto.PrescricaoResponse;
import br.gov.farmasus.medication.model.TipoUsuario;
import br.gov.farmasus.medication.model.UsuarioPrincipal;
import br.gov.farmasus.medication.service.PacienteLoginMapper;
import br.gov.farmasus.medication.service.PrescricaoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/{pacienteId}/medicamentos")
public class PrescricaoController {
  private final PrescricaoService prescricaoService;
  private final PacienteLoginMapper pacienteLoginMapper;

  public PrescricaoController(PrescricaoService prescricaoService, PacienteLoginMapper pacienteLoginMapper) {
    this.prescricaoService = prescricaoService;
    this.pacienteLoginMapper = pacienteLoginMapper;
  }

  @PostMapping
  public ResponseEntity<PrescricaoResponse> criarPrescricao(
      @PathVariable Long pacienteId,
      @Valid @RequestBody PrescricaoRequest request) {
    validarAcessoPaciente(pacienteId);
    PrescricaoResponse response = prescricaoService.criarPrescricao(pacienteId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<PrescricaoResponse>> listarPrescricoes(@PathVariable Long pacienteId) {
    validarAcessoPaciente(pacienteId);
    return ResponseEntity.ok(prescricaoService.listarPrescricoes(pacienteId));
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
