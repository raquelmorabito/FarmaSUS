package br.gov.farmasus.safety.config;

import br.gov.farmasus.safety.domain.InteracaoMedicamento;
import br.gov.farmasus.safety.domain.Severidade;
import br.gov.farmasus.safety.repository.InteracaoMedicamentoRepository;
import br.gov.farmasus.safety.service.InteracaoMedicamentoService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InteracaoSeedConfig {
  @Bean
  public CommandLineRunner seedInteracoes(
      InteracaoMedicamentoRepository repository,
      InteracaoMedicamentoService interacaoService) {
    return args -> {
      if (repository.count() > 0) {
        return;
      }

      List<InteracaoMedicamento> interacoes = List.of(
          criar(interacaoService, "varfarina", "ibuprofeno", Severidade.ALTA,
              "Risco de sangramento aumentado.", "Evitar associacao ou monitorar INR."),
          criar(interacaoService, "clopidogrel", "omeprazol", Severidade.MODERADA,
              "Reducao da eficacia do clopidogrel.", "Preferir pantoprazol."),
          criar(interacaoService, "metformina", "contraste iodado", Severidade.ALTA,
              "Risco de acidose lactica.", "Suspender metformina antes do exame."),
          criar(interacaoService, "sinvastatina", "claritromicina", Severidade.CONTRAINDICADA,
              "Risco de rabdomiolise.", "Nao associar ou suspender estatina."),
          criar(interacaoService, "enalapril", "espironolactona", Severidade.MODERADA,
              "Risco de hiperpotassemia.", "Monitorar potassio."),
          criar(interacaoService, "losartana", "suplemento de potassio", Severidade.MODERADA,
              "Risco de hiperpotassemia.", "Evitar suplementacao sem controle."),
          criar(interacaoService, "fluoxetina", "tramadol", Severidade.ALTA,
              "Risco de sindrome serotoninergica.", "Evitar combinacao."),
          criar(interacaoService, "amiodarona", "digoxina", Severidade.ALTA,
              "Aumenta niveis de digoxina.", "Ajustar dose e monitorar."),
          criar(interacaoService, "rifampicina", "anticoncepcional oral", Severidade.ALTA,
              "Diminui eficacia do anticoncepcional.", "Usar metodo alternativo."),
          criar(interacaoService, "ciprofloxacino", "antiacido", Severidade.BAIXA,
              "Diminui absorcao.", "Separar horarios de uso."),
          criar(interacaoService, "levotiroxina", "suplemento de ferro", Severidade.MODERADA,
              "Reduz absorcao da levotiroxina.", "Tomar em horarios diferentes."),
          criar(interacaoService, "sertralina", "linezolida", Severidade.CONTRAINDICADA,
              "Risco elevado de sindrome serotoninergica.", "Nao associar."),
          criar(interacaoService, "insulina", "propranolol", Severidade.MODERADA,
              "Mascaramento de hipoglicemia.", "Monitorar glicemia."),
          criar(interacaoService, "fenitoina", "fluconazol", Severidade.ALTA,
              "Aumenta niveis de fenitoina.", "Monitorar niveis e ajustar dose.")
      );

      repository.saveAll(interacoes);
    };
  }

  private InteracaoMedicamento criar(
      InteracaoMedicamentoService interacaoService,
      String med1,
      String med2,
      Severidade severidade,
      String mensagem,
      String recomendacao) {
    String nome1 = interacaoService.normalizar(med1);
    String nome2 = interacaoService.normalizar(med2);
    String primeiro = nome1.compareTo(nome2) <= 0 ? nome1 : nome2;
    String segundo = nome1.compareTo(nome2) <= 0 ? nome2 : nome1;

    InteracaoMedicamento interacao = new InteracaoMedicamento();
    interacao.setMedicamentoA(primeiro);
    interacao.setMedicamentoB(segundo);
    interacao.setSeveridade(severidade);
    interacao.setMensagem(mensagem);
    interacao.setRecomendacao(recomendacao);
    return interacao;
  }
}
