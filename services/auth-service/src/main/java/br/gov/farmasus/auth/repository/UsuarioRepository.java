package br.gov.farmasus.auth.repository;

import br.gov.farmasus.auth.domain.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, String> {
  Optional<UsuarioEntity> findByLogin(String login);
}
