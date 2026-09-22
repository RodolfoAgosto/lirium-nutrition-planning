package com.lirium.nutrition.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.expiration}")
  private long expiration;

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(UserDetails userDetails) {
    List<String> roles =
        userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    return Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setSubject(userDetails.getUsername())
        .claim("roles", roles) // lista real, no el toString()
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getKey())
        .compact();
  }

  public String extractUsername(String token) {
    return getClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  /**
   * Parses the token once so callers (e.g. the JWT filter) can reuse the resulting Claims instead
   * of triggering a fresh signature verification + parse per check.
   */
  public Claims parseClaims(String token) {
    return getClaims(token);
  }

  public String extractUsername(Claims claims) {
    return claims.getSubject();
  }

  public boolean isTokenValid(Claims claims, UserDetails userDetails) {
    return claims.getSubject().equals(userDetails.getUsername()) && !isTokenExpired(claims);
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
  }

  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  private boolean isTokenExpired(Claims claims) {
    return claims.getExpiration().before(new Date());
  }

  public String generateExpiredToken(UserDetails userDetails) {
    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis() - 1000000))
        .setExpiration(new Date(System.currentTimeMillis() - 500000))
        .signWith(getKey())
        .compact();
  }
}
