package br.gov.farmasus.medication.dto;

public record VerificacaoInteracaoResponse(
    boolean possuiRisco,
    String severidade,
    String mensagem,
    String recomendacao,
    String medicamentoConflitante
) {}

