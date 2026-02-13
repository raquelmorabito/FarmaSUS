package br.gov.farmasus.patient.controller;

import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.dto.CartaoMedicacaoResponse;
import br.gov.farmasus.patient.model.TipoUsuario;
import br.gov.farmasus.patient.model.UsuarioPrincipal;
import br.gov.farmasus.patient.service.AlertaPacienteService;
import br.gov.farmasus.patient.service.CartaoMedicacaoService;
import br.gov.farmasus.patient.service.PacienteLoginMapper;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/me")
public class PacienteMeController {
  private final CartaoMedicacaoService cartaoMedicacaoService;
  private final AlertaPacienteService alertaPacienteService;
  private final PacienteLoginMapper pacienteLoginMapper;

  public PacienteMeController(
      CartaoMedicacaoService cartaoMedicacaoService,
      AlertaPacienteService alertaPacienteService,
      PacienteLoginMapper pacienteLoginMapper) {
    this.cartaoMedicacaoService = cartaoMedicacaoService;
    this.alertaPacienteService = alertaPacienteService;
    this.pacienteLoginMapper = pacienteLoginMapper;
  }

  @GetMapping("/cartao-medicacao")
  public ResponseEntity<CartaoMedicacaoResponse> obterMeuCartao(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(cartaoMedicacaoService.obterCartao(pacienteId, authorizationHeader));
  }

  @GetMapping("/alertas")
  public ResponseEntity<List<AlertaPacienteResponse>> listarMeusAlertas() {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(alertaPacienteService.listarAlertas(pacienteId));
  }

  private Long resolverPacienteLogado() {
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

    if (tipoUsuario != TipoUsuario.PACIENTE) {
      throw new AccessDeniedException("Endpoint /me disponivel apenas para paciente.");
    }

    return pacienteLoginMapper.obterPacienteId(principal.login())
        .orElseThrow(() -> new AccessDeniedException("Paciente nao associado ao login autenticado."));
  }
}
