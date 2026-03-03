package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {

    Optional<Restriction> findByCode(String code);

    List<Restriction> findByCategory(RestrictionCategory category);

    List<Restriction> findByNameContainingIgnoreCase(String namePart);

}