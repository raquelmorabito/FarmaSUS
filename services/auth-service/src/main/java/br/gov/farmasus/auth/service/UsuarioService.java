package br.gov.farmasus.auth.service;

import br.gov.farmasus.auth.domain.UsuarioEntity;
import br.gov.farmasus.auth.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
  private final UsuarioRepository usuarioRepository;

  public UsuarioService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  public Optional<UsuarioEntity> buscarPorLogin(String login) {
    return usuarioRepository.findByLogin(login);
  }
}
