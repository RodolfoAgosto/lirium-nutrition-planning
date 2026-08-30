package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  public Optional<RefreshToken> findByUser(User user);

  public Optional<RefreshToken> findByToken(String token);

  public Optional<RefreshToken> findByUserAndRevokedFalse(User user);
}
