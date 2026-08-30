package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByDni(String dni);

  Optional<User> findByEmailAndEnabledTrue(String email);

  Boolean existsByEmail(String email);

  boolean existsByDni(String dni);
}
