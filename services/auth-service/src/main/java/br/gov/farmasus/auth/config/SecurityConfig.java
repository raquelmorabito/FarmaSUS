package br.gov.farmasus.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .httpBasic(b -> b.disable())
        .formLogin(f -> f.disable())
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((req, res, e) -> {
              res.setStatus(401);
              res.setCharacterEncoding("UTF-8");
              res.setContentType("application/json;charset=UTF-8");
              res.getWriter().write("{\"erro\":\"Nao autorizado\"}");
            })
            .accessDeniedHandler((req, res, e) -> {
              res.setStatus(403);
              res.setCharacterEncoding("UTF-8");
              res.setContentType("application/json;charset=UTF-8");
              res.getWriter().write("{\"erro\":\"Proibido\"}");
            })
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/auth/login",
                "/actuator",
                "/actuator/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/error"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
