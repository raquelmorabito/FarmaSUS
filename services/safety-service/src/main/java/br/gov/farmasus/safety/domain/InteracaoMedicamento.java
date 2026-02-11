package br.gov.farmasus.safety.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interacoes_medicamento")
public class InteracaoMedicamento {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "medicamento_a", nullable = false)
  private String medicamentoA;

  @Column(name = "medicamento_b", nullable = false)
  private String medicamentoB;

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

  public String getMedicamentoA() {
    return medicamentoA;
  }

  public void setMedicamentoA(String medicamentoA) {
    this.medicamentoA = medicamentoA;
  }

  public String getMedicamentoB() {
    return medicamentoB;
  }

  public void setMedicamentoB(String medicamentoB) {
    this.medicamentoB = medicamentoB;
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
