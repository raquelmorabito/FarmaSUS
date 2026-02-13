package br.gov.farmasus.safety.dto;

import br.gov.farmasus.safety.domain.Severidade;

public record VerificacaoInteracaoResponse(
    boolean possuiRisco,
    Severidade severidade,
    String mensagem,
    String recomendacao,
    String medicamentoConflitante
) {}

