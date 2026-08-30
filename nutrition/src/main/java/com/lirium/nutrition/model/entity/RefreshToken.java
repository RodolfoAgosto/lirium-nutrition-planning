package com.lirium.nutrition.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token", nullable = false, unique = true)
  private String token;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  public RefreshToken(User user, String token, Instant expiresAt) {
    this.user = user;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public void revoke() {
    this.revoked = true;
  }
}
