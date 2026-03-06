package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);

    Optional<User> findByEmailAndEnabledTrue(String email);

    Boolean existsByEmail(String email);

}