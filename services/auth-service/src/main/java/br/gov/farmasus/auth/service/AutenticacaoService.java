package br.gov.farmasus.auth.service;

import br.gov.farmasus.auth.dto.LoginRequest;
import br.gov.farmasus.auth.dto.LoginResponse;
import br.gov.farmasus.auth.model.Usuario;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutenticacaoService {
  private final UsuarioService usuarioService;
  private final JwtService jwtService;

  public AutenticacaoService(UsuarioService usuarioService, JwtService jwtService) {
    this.usuarioService = usuarioService;
    this.jwtService = jwtService;
  }

  public LoginResponse autenticar(LoginRequest request) {
    Usuario usuario = usuarioService.buscarPorLogin(request.login())
        .filter(u -> u.tipoUsuario().equals(request.tipoUsuario()))
        .filter(u -> u.senha().equals(request.senha()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

    String token = jwtService.gerarToken(usuario.login(), usuario.tipoUsuario().name());
    OffsetDateTime expiraEm = jwtService.calcularExpiracao();
    return new LoginResponse(token, usuario.tipoUsuario().name(), expiraEm);
  }
}
