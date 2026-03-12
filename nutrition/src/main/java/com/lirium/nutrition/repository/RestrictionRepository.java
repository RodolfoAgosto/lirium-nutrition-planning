package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {

    Optional<Restriction> findById(Long id);

    @Query("SELECT r FROM Restriction r WHERE r.code IN :codes")
    Set<Restriction> findByCodes(@Param("codes") Set<String> codes);

    List<Restriction> findByCategory(RestrictionCategory category);

    List<Restriction> findByNameContainingIgnoreCase(String namePart);

}