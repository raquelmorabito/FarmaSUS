package br.gov.farmasus.safety.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "prescricoes_paciente")
public class PrescricaoPaciente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "prescricao_id", nullable = false)
  private Long prescricaoId;

  @Column(name = "paciente_id", nullable = false)
  private Long pacienteId;

  @Column(name = "nome_medicamento", nullable = false)
  private String nomeMedicamento;

  @Column(nullable = false)
  private LocalDate inicio;

  @Column(nullable = false)
  private LocalDate fim;

  public Long getId() {
    return id;
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

  public LocalDate getInicio() {
    return inicio;
  }

  public void setInicio(LocalDate inicio) {
    this.inicio = inicio;
  }

  public LocalDate getFim() {
    return fim;
  }

  public void setFim(LocalDate fim) {
    this.fim = fim;
  }
}
