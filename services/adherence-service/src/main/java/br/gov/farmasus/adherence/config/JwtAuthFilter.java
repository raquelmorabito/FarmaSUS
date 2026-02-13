package br.gov.farmasus.adherence.config;

import br.gov.farmasus.adherence.model.UsuarioPrincipal;
import br.gov.farmasus.adherence.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = extrairToken(request.getHeader(HttpHeaders.AUTHORIZATION));
    if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        Claims claims = jwtService.validarToken(token);
        String login = claims.getSubject();
        String tipoUsuario = claims.get("tipoUsuario", String.class);
        if (login != null && tipoUsuario != null) {
          UsuarioPrincipal principal = new UsuarioPrincipal(login, tipoUsuario);
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              principal,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario))
          );
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception ex) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  private String extrairToken(String authorizationHeader) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      return null;
    }
    if (!authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
      return null;
    }
    String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    return token.isEmpty() ? null : token;
  }
}
