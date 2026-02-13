package br.gov.farmasus.patient.controller;

import br.gov.farmasus.patient.domain.OrigemAlergia;
import br.gov.farmasus.patient.dto.AlergiaPacienteRequest;
import br.gov.farmasus.patient.dto.AlergiaPacienteResponse;
import br.gov.farmasus.patient.dto.PerfilClinicoPacienteResponse;
import br.gov.farmasus.patient.dto.TipoSanguineoRequest;
import br.gov.farmasus.patient.model.TipoUsuario;
import br.gov.farmasus.patient.model.UsuarioPrincipal;
import br.gov.farmasus.patient.service.PerfilClinicoPacienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/{pacienteId}")
public class PacienteProfissionalController {
  private final PerfilClinicoPacienteService perfilClinicoPacienteService;

  public PacienteProfissionalController(PerfilClinicoPacienteService perfilClinicoPacienteService) {
    this.perfilClinicoPacienteService = perfilClinicoPacienteService;
  }

  @GetMapping("/perfil-clinico")
  public ResponseEntity<PerfilClinicoPacienteResponse> obterPerfil(@PathVariable Long pacienteId) {
    validarProfissional();
    return ResponseEntity.ok(perfilClinicoPacienteService.obterPerfil(pacienteId));
  }

  @PutMapping("/tipo-sanguineo")
  public ResponseEntity<PerfilClinicoPacienteResponse> definirTipoSanguineo(
      @PathVariable Long pacienteId,
      @Valid @RequestBody TipoSanguineoRequest request) {
    validarProfissional();
    return ResponseEntity.ok(perfilClinicoPacienteService.definirTipoSanguineo(pacienteId, request));
  }

  @GetMapping("/alergias")
  public ResponseEntity<List<AlergiaPacienteResponse>> listarAlergias(@PathVariable Long pacienteId) {
    validarProfissional();
    return ResponseEntity.ok(perfilClinicoPacienteService.listarAlergias(pacienteId));
  }

  @PostMapping("/alergias")
  public ResponseEntity<AlergiaPacienteResponse> adicionarAlergia(
      @PathVariable Long pacienteId,
      @Valid @RequestBody AlergiaPacienteRequest request) {
    validarProfissional();
    return ResponseEntity.ok(
        perfilClinicoPacienteService.criarAlergia(pacienteId, request, OrigemAlergia.PROFISSIONAL));
  }

  @PutMapping("/alergias/{alergiaId}")
  public ResponseEntity<AlergiaPacienteResponse> atualizarAlergia(
      @PathVariable Long pacienteId,
      @PathVariable Long alergiaId,
      @Valid @RequestBody AlergiaPacienteRequest request) {
    validarProfissional();
    return ResponseEntity.ok(perfilClinicoPacienteService.atualizarAlergia(pacienteId, alergiaId, request));
  }

  @DeleteMapping("/alergias/{alergiaId}")
  public ResponseEntity<Void> removerAlergia(@PathVariable Long pacienteId, @PathVariable Long alergiaId) {
    validarProfissional();
    perfilClinicoPacienteService.removerAlergia(pacienteId, alergiaId);
    return ResponseEntity.noContent().build();
  }

  private void validarProfissional() {
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

    if (tipoUsuario != TipoUsuario.PROFISSIONAL) {
      throw new AccessDeniedException("Operacao permitida apenas para profissional.");
    }
  }
}

