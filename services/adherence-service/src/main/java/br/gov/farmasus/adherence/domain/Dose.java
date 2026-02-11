package br.gov.farmasus.adherence.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "doses",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_dose_prescricao_data_horario",
            columnNames = {"prescricao_id", "data", "horario"}))
public class Dose {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "prescricao_id", nullable = false)
  private Long prescricaoId;

  @Column(name = "paciente_id", nullable = false)
  private Long pacienteId;

  @Column(nullable = false)
  private String nomeMedicamento;

  @Column(nullable = false)
  private LocalDate data;

  @Column(nullable = false)
  private LocalTime horario;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DoseStatus status;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getPrescricaoId() {
    return prescricaoId;
  }

  public void setPrescricaoId(Long prescricaoId) {
    this.prescricaoId = prescricaoId;
  }

  public Long getPacienteId() {
    return pacienteId;
  }

  public void setPacienteId(Long pacienteId) {
    this.pacienteId = pacienteId;
  }

  public String getNomeMedicamento() {
    return nomeMedicamento;
  }

  public void setNomeMedicamento(String nomeMedicamento) {
    this.nomeMedicamento = nomeMedicamento;
  }

  public LocalDate getData() {
    return data;
  }

  public void setData(LocalDate data) {
    this.data = data;
  }

  public LocalTime getHorario() {
    return horario;
  }

  public void setHorario(LocalTime horario) {
    this.horario = horario;
  }

  public DoseStatus getStatus() {
    return status;
  }

  public void setStatus(DoseStatus status) {
    this.status = status;
  }
}
