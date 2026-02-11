package br.gov.farmasus.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  private final SecretKey secretKey;
  private final int expiracaoMinutos;

  public JwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiracao-minutos}") int expiracaoMinutos) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiracaoMinutos = expiracaoMinutos;
  }

  public String gerarToken(String login, String tipoUsuario) {
    OffsetDateTime agora = OffsetDateTime.now(ZONA_BR);
    OffsetDateTime expiraEm = agora.plusMinutes(expiracaoMinutos);
    return Jwts.builder()
        .setSubject(login)
        .claim("tipoUsuario", tipoUsuario)
        .setIssuedAt(Date.from(agora.toInstant()))
        .setExpiration(Date.from(expiraEm.toInstant()))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims validarToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public OffsetDateTime calcularExpiracao() {
    return OffsetDateTime.now(ZONA_BR).plusMinutes(expiracaoMinutos);
  }
}
