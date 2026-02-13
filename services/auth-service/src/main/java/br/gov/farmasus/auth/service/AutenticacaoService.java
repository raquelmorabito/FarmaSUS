package br.gov.farmasus.auth.service;

import br.gov.farmasus.auth.dto.LoginRequest;
import br.gov.farmasus.auth.dto.LoginResponse;
import br.gov.farmasus.auth.domain.UsuarioEntity;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutenticacaoService {
  private final UsuarioService usuarioService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AutenticacaoService(
      UsuarioService usuarioService,
      JwtService jwtService,
      PasswordEncoder passwordEncoder) {
    this.usuarioService = usuarioService;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  }

  public LoginResponse autenticar(LoginRequest request) {
    UsuarioEntity usuario = usuarioService.buscarPorLogin(request.login())
        .filter(u -> u.getTipoUsuario().equals(request.tipoUsuario()))
        .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

    String token = jwtService.gerarToken(usuario.getLogin(), usuario.getTipoUsuario().name());
    OffsetDateTime expiraEm = jwtService.calcularExpiracao();
    return new LoginResponse(token, usuario.getTipoUsuario().name(), expiraEm);
  }
}
