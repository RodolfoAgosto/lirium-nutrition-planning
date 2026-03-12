package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findAllByOrderByNameAsc(String name);

    Set<Food> findByFoodTagsIn(Set<FoodTag> tags);

    boolean existsByName(String name);
}