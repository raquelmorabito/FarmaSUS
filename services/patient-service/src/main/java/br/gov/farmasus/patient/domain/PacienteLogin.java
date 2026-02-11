package br.gov.farmasus.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "paciente_login",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_paciente_login_login", columnNames = {"login"}),
      @UniqueConstraint(name = "uk_paciente_login_paciente_id", columnNames = {"paciente_id"})
    }
)
public class PacienteLogin {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String login;

  @Column(name = "paciente_id", nullable = false, unique = true)
  private Long pacienteId;

  public Long getId() {
    return id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public Long getPacienteId() {
    return pacienteId;
  }

  public void setPacienteId(Long pacienteId) {
    this.pacienteId = pacienteId;
  }
}
