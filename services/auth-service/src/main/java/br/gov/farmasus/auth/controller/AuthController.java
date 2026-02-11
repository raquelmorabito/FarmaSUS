package br.gov.farmasus.auth.controller;

import br.gov.farmasus.auth.dto.LoginRequest;
import br.gov.farmasus.auth.dto.LoginResponse;
import br.gov.farmasus.auth.dto.MeResponse;
import br.gov.farmasus.auth.model.UsuarioPrincipal;
import br.gov.farmasus.auth.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AutenticacaoService autenticacaoService;

  public AuthController(AutenticacaoService autenticacaoService) {
    this.autenticacaoService = autenticacaoService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(autenticacaoService.autenticar(request));
  }

  @GetMapping("/me")
  public ResponseEntity<MeResponse> me(Authentication authentication) {
    UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
    return ResponseEntity.ok(new MeResponse(principal.login(), principal.tipoUsuario()));
  }
}
