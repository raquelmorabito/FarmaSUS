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
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "alertas_paciente",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_alerta_paciente_event", columnNames = {"event_id"})
    },
    indexes = {
      @Index(name = "idx_alerta_paciente_paciente", columnList = "paciente_id")
    }
)
public class AlertaPaciente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @Column(name = "correlation_id", nullable = false)
  private UUID correlationId;

  @Column(name = "criado_em", nullable = false)
  private OffsetDateTime criadoEm;

  @Column(name = "alerta_id", nullable = false)
  private Long alertaId;

  @Column(name = "paciente_id", nullable = false)
  private Long pacienteId;

  @Column(name = "prescricao_id", nullable = false)
  private Long prescricaoId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Severidade severidade;

  @Column(nullable = false, length = 500)
  private String mensagem;

  @Column(nullable = false, length = 500)
  private String recomendacao;

  public Long getId() {
    return id;
  }

  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public UUID getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(UUID correlationId) {
    this.correlationId = correlationId;
  }

  public OffsetDateTime getCriadoEm() {
    return criadoEm;
  }

  public void setCriadoEm(OffsetDateTime criadoEm) {
    this.criadoEm = criadoEm;
  }

  public Long getAlertaId() {
    return alertaId;
  }

  public void setAlertaId(Long alertaId) {
    this.alertaId = alertaId;
  }

  public Long getPacienteId() {
    return pacienteId;
  }

  public void setPacienteId(Long pacienteId) {
    this.pacienteId = pacienteId;
  }

  public Long getPrescricaoId() {
    return prescricaoId;
  }

  public void setPrescricaoId(Long prescricaoId) {
    this.prescricaoId = prescricaoId;
  }

  public Severidade getSeveridade() {
    return severidade;
  }

  public void setSeveridade(Severidade severidade) {
    this.severidade = severidade;
  }

  public String getMensagem() {
    return mensagem;
  }

  public void setMensagem(String mensagem) {
    this.mensagem = mensagem;
  }

  public String getRecomendacao() {
    return recomendacao;
  }

  public void setRecomendacao(String recomendacao) {
    this.recomendacao = recomendacao;
  }
}
