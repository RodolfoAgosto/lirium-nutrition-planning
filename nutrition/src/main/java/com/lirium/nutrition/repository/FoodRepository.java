package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findAllByOrderByNameAsc(String name);

    Set<Food> findByFoodTagsIn(Set<FoodTag> tags);

    boolean existsByName(String name);

    Optional<Food> findByName(String name);

    @Query("""
    SELECT f FROM Food f
    WHERE :mealType MEMBER OF f.suitableFor
    AND NOT EXISTS (
        SELECT t FROM Food f2 JOIN f2.foodTags t
        WHERE f2 = f AND t IN :excludedTags
    )
    """)
    List<Food> findSuitableFoods(
            @Param("mealType") MealType mealType,
            @Param("excludedTags") Set<FoodTag> excludedTags
    );

}