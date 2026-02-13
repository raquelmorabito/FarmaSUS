package br.gov.farmasus.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "perfil_paciente",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_perfil_paciente_paciente_id", columnNames = {"paciente_id"})
    },
    indexes = {
      @Index(name = "idx_perfil_paciente_id", columnList = "paciente_id")
    }
)
public class PerfilPaciente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "paciente_id", nullable = false, unique = true)
  private Long pacienteId;

  @Column(name = "tipo_sanguineo", length = 5)
  private String tipoSanguineo;

  public Long getId() {
    return id;
  }

  public Long getPacienteId() {
    return pacienteId;
  }

  public void setPacienteId(Long pacienteId) {
    this.pacienteId = pacienteId;
  }

  public String getTipoSanguineo() {
    return tipoSanguineo;
  }

  public void setTipoSanguineo(String tipoSanguineo) {
    this.tipoSanguineo = tipoSanguineo;
  }
}

