package br.gov.farmasus.patient.controller;

import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.dto.AlergiaPacienteRequest;
import br.gov.farmasus.patient.dto.AlergiaPacienteResponse;
import br.gov.farmasus.patient.dto.CartaoMedicacaoResponse;
import br.gov.farmasus.patient.dto.DisponibilidadeMedicamentoResponse;
import br.gov.farmasus.patient.dto.LembretePacienteResponse;
import br.gov.farmasus.patient.dto.PerfilClinicoPacienteResponse;
import br.gov.farmasus.patient.model.TipoUsuario;
import br.gov.farmasus.patient.model.UsuarioPrincipal;
import br.gov.farmasus.patient.domain.OrigemAlergia;
import br.gov.farmasus.patient.service.AlertaPacienteService;
import br.gov.farmasus.patient.service.CartaoMedicacaoService;
import br.gov.farmasus.patient.service.DisponibilidadeUbsService;
import br.gov.farmasus.patient.service.LembretePacienteService;
import br.gov.farmasus.patient.service.PacienteLoginMapper;
import br.gov.farmasus.patient.service.PerfilClinicoPacienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes/me")
public class PacienteMeController {
  private final CartaoMedicacaoService cartaoMedicacaoService;
  private final AlertaPacienteService alertaPacienteService;
  private final LembretePacienteService lembretePacienteService;
  private final DisponibilidadeUbsService disponibilidadeUbsService;
  private final PerfilClinicoPacienteService perfilClinicoPacienteService;
  private final PacienteLoginMapper pacienteLoginMapper;

  public PacienteMeController(
      CartaoMedicacaoService cartaoMedicacaoService,
      AlertaPacienteService alertaPacienteService,
      LembretePacienteService lembretePacienteService,
      DisponibilidadeUbsService disponibilidadeUbsService,
      PerfilClinicoPacienteService perfilClinicoPacienteService,
      PacienteLoginMapper pacienteLoginMapper) {
    this.cartaoMedicacaoService = cartaoMedicacaoService;
    this.alertaPacienteService = alertaPacienteService;
    this.lembretePacienteService = lembretePacienteService;
    this.disponibilidadeUbsService = disponibilidadeUbsService;
    this.perfilClinicoPacienteService = perfilClinicoPacienteService;
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

  @GetMapping("/lembretes")
  public ResponseEntity<List<LembretePacienteResponse>> listarMeusLembretes(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(lembretePacienteService.listarLembretes(pacienteId, authorizationHeader));
  }

  @GetMapping("/disponibilidade-ubs")
  public ResponseEntity<DisponibilidadeMedicamentoResponse> buscarDisponibilidade(
      @RequestParam String medicamento) {
    resolverPacienteLogado();
    return ResponseEntity.ok(disponibilidadeUbsService.buscar(medicamento));
  }

  @GetMapping("/perfil-clinico")
  public ResponseEntity<PerfilClinicoPacienteResponse> obterMeuPerfil() {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(perfilClinicoPacienteService.obterPerfil(pacienteId));
  }

  @GetMapping("/alergias")
  public ResponseEntity<List<AlergiaPacienteResponse>> listarMinhasAlergias() {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(perfilClinicoPacienteService.listarAlergias(pacienteId));
  }

  @PostMapping("/alergias")
  public ResponseEntity<AlergiaPacienteResponse> adicionarAlergia(
      @Valid @RequestBody AlergiaPacienteRequest request) {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(perfilClinicoPacienteService.criarAlergia(pacienteId, request, OrigemAlergia.PACIENTE));
  }

  @PutMapping("/alergias/{alergiaId}")
  public ResponseEntity<AlergiaPacienteResponse> atualizarAlergia(
      @PathVariable Long alergiaId,
      @Valid @RequestBody AlergiaPacienteRequest request) {
    Long pacienteId = resolverPacienteLogado();
    return ResponseEntity.ok(perfilClinicoPacienteService.atualizarAlergia(pacienteId, alergiaId, request));
  }

  @DeleteMapping("/alergias/{alergiaId}")
  public ResponseEntity<Void> removerAlergia(@PathVariable Long alergiaId) {
    Long pacienteId = resolverPacienteLogado();
    perfilClinicoPacienteService.removerAlergia(pacienteId, alergiaId);
    return ResponseEntity.noContent().build();
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
