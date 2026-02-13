package br.gov.farmasus.patient.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

  @Bean
  public RestTemplate restTemplate(
      RestTemplateBuilder builder,
      @Value("${http.client.connect-timeout-ms:2000}") long connectTimeoutMs,
      @Value("${http.client.read-timeout-ms:5000}") long readTimeoutMs) {
    return builder
        .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
        .setReadTimeout(Duration.ofMillis(readTimeoutMs))
        .build();
  }
}
