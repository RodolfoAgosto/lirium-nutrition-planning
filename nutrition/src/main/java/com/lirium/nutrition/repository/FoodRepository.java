package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findByName(String name);

    List<Food> findByFoodTags(FoodTag tag);

    void deleteByName(String name);

}