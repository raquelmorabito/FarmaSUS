package br.gov.farmasus.medication.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescricoes")
public class Prescricao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "paciente_id", nullable = false)
  private Long pacienteId;

  @Column(name = "nome_medicamento", nullable = false)
  private String nomeMedicamento;

  @Column(nullable = false)
  private String dosagem;

  @Column(name = "frequencia_diaria", nullable = false)
  private Integer frequenciaDiaria;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "prescricao_horarios", joinColumns = @JoinColumn(name = "prescricao_id"))
  @Column(name = "horario", nullable = false)
  private List<LocalTime> horarios = new ArrayList<>();

  @Column(nullable = false)
  private LocalDate inicio;

  @Column(nullable = false)
  private LocalDate fim;

  public Long getId() {
    return id;
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

  public String getDosagem() {
    return dosagem;
  }

  public void setDosagem(String dosagem) {
    this.dosagem = dosagem;
  }

  public Integer getFrequenciaDiaria() {
    return frequenciaDiaria;
  }

  public void setFrequenciaDiaria(Integer frequenciaDiaria) {
    this.frequenciaDiaria = frequenciaDiaria;
  }

  public List<LocalTime> getHorarios() {
    return horarios;
  }

  public void setHorarios(List<LocalTime> horarios) {
    this.horarios = horarios;
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
