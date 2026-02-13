package br.gov.farmasus.medication.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobs.receita-perto-vencer")
public record ReceitaPertoVencerProperties(
    String cron,
    String zone,
    int diasAntecedencia) {
}
