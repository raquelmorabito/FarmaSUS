package br.gov.farmasus.patient.dto;

public record UbsDisponibilidadeResponse(
    Long ubsId,
    String nomeUbs,
    String endereco,
    Integer quantidadeDisponivel
) {}

