package br.gov.farmasus.adherence.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "registros_dose")
public class RegistroDose {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "dose_id")
  private Dose dose;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DoseStatus status;

  @Column(nullable = false)
  private OffsetDateTime registradoEm;

  @Column(length = 300)
  private String comentario;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Dose getDose() {
    return dose;
  }

  public void setDose(Dose dose) {
    this.dose = dose;
  }

  public DoseStatus getStatus() {
    return status;
  }

  public void setStatus(DoseStatus status) {
    this.status = status;
  }

  public OffsetDateTime getRegistradoEm() {
    return registradoEm;
  }

  public void setRegistradoEm(OffsetDateTime registradoEm) {
    this.registradoEm = registradoEm;
  }

  public String getComentario() {
    return comentario;
  }

  public void setComentario(String comentario) {
    this.comentario = comentario;
  }
}
