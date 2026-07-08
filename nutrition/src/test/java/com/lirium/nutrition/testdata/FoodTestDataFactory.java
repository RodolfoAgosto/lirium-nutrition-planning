package com.lirium.nutrition.testdata;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FoodTestDataFactory {

    private final FoodRepository foodRepository;


    public void createAllRequiredFoods() {

        foodRepository.save(Food.of(
                "Leche",
                60, 3, 5, 3,
                FoodCategory.DAIRY,
                Set.of(
                        MealType.BREAKFAST,
                        MealType.MID_MORNING,
                        MealType.SNACK
                )
        ));

        foodRepository.save(Food.of(
                "Arroz",
                130, 3, 28, 1,
                FoodCategory.CARB,
                Set.of(
                        MealType.BREAKFAST,
                        MealType.LUNCH,
                        MealType.DINNER,
                        MealType.SNACK
                )
        ));

        foodRepository.save(Food.of(
                "Pollo",
                165, 31, 0, 3,
                FoodCategory.PROTEIN,
                Set.of(
                        MealType.LUNCH,
                        MealType.DINNER
                )
        ));

        foodRepository.save(Food.of(
                "Lechuga",
                15, 1, 3, 0,
                FoodCategory.VEGETABLE,
                Set.of(
                        MealType.LUNCH,
                        MealType.DINNER
                )
        ));

        foodRepository.save(Food.of(
                "Manzana",
                52, 0, 14, 0,
                FoodCategory.FRUIT,
                Set.of(
                        MealType.MID_MORNING,
                        MealType.DINNER
                )
        ));

        foodRepository.save(Food.of(
                "Miel",
                304, 0, 82, 0,
                FoodCategory.SWEET,
                Set.of(
                        MealType.LUNCH
                )
        ));

        foodRepository.save(Food.ofLiquid(
                "Agua",
                0,0,0,0,
                FoodCategory.BEVERAGE,
                Set.of(
                        MealType.BREAKFAST,
                        MealType.LUNCH,
                        MealType.DINNER,
                        MealType.SNACK
                ),
                1.0
        ));
    }
}