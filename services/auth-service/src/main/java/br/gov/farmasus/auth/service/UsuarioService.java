package br.gov.farmasus.auth.service;

import br.gov.farmasus.auth.model.TipoUsuario;
import br.gov.farmasus.auth.model.Usuario;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
  private final Map<String, Usuario> usuarios = Map.of(
      "paciente1", new Usuario("paciente1", "senha123", TipoUsuario.PACIENTE),
      "prof1", new Usuario("prof1", "senha123", TipoUsuario.PROFISSIONAL)
  );

  public Optional<Usuario> buscarPorLogin(String login) {
    return Optional.ofNullable(usuarios.get(login));
  }
}
