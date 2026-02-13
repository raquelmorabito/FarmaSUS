package br.gov.farmasus.auth.domain;

import br.gov.farmasus.auth.model.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {
  @Id
  @Column(nullable = false, length = 100)
  private String login;

  @Column(name = "senha_hash", nullable = false, length = 120)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_usuario", nullable = false, length = 30)
  private TipoUsuario tipoUsuario;

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public void setSenhaHash(String senhaHash) {
    this.senhaHash = senhaHash;
  }

  public TipoUsuario getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(TipoUsuario tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }
}
