package br.gov.farmasus.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "alergias_paciente",
    indexes = {
      @Index(name = "idx_alergias_paciente_id", columnList = "paciente_id")
    }
)
public class AlergiaPaciente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "paciente_id", nullable = false)
  private Long pacienteId;

  @Column(nullable = false, length = 200)
  private String descricao;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrigemAlergia origem;

  public Long getId() {
    return id;
  }

  public Long getPacienteId() {
    return pacienteId;
  }

  public void setPacienteId(Long pacienteId) {
    this.pacienteId = pacienteId;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public OrigemAlergia getOrigem() {
    return origem;
  }

  public void setOrigem(OrigemAlergia origem) {
    this.origem = origem;
  }
}

