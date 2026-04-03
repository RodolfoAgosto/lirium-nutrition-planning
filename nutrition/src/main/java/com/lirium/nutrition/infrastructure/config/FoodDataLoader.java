package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(3)
public class FoodDataLoader implements CommandLineRunner {

    private final FoodRepository foodRepository;

    @Override
    @Transactional
    public void run(String... args) {

        if (foodRepository.count() > 0) return;

        foodRepository.saveAll(List.of(

                // ── PROTEINS ──────────────────────────────────────────────

                Food.of("Chicken Breast", 165, 31, 0, 4,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Salmon", 208, 20, 0, 13,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Tuna in Water", 116, 26, 0, 1,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Ground Beef", 254, 17, 0, 20,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Tofu", 76, 8, 2, 5,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                // Huevo con UNIT — 1 huevo = 60g
                Food.ofUnit("Egg", 155, 13, 1, 11,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.BREAKFAST, MealType.LUNCH),
                        60.0),

                // ── DAIRY ─────────────────────────────────────────────────

                Food.of("Whole Milk Yogurt", 61, 3, 5, 3,
                        FoodCategory.DAIRY,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING)),

                Food.of("Cottage Cheese", 98, 11, 3, 4,
                        FoodCategory.DAIRY,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING)),

                Food.of("Greek Yogurt", 100, 10, 4, 5,
                        FoodCategory.DAIRY,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING)),

                // Leche con MILLILITER — densidad 1.03
                Food.ofLiquid("Whole Milk", 61, 3, 5, 3,
                        FoodCategory.DAIRY,
                        Set.of(MealType.BREAKFAST, MealType.SNACK),
                        1.03),

                // ── CARBS ─────────────────────────────────────────────────

                Food.of("Oatmeal", 389, 17, 66, 7,
                        FoodCategory.CARB,
                        Set.of(MealType.BREAKFAST)),

                Food.of("Brown Rice", 216, 5, 45, 2,
                        FoodCategory.CARB,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Whole Wheat Pasta", 348, 13, 67, 3,
                        FoodCategory.CARB,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Sweet Potato", 86, 2, 20, 0,
                        FoodCategory.CARB,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Whole Grain Bread", 247, 9, 41, 4,
                        FoodCategory.CARB,
                        Set.of(MealType.BREAKFAST, MealType.SNACK)),

                Food.of("Quinoa", 120, 4, 22, 2,
                        FoodCategory.CARB,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                // ── VEGETABLES ────────────────────────────────────────────

                Food.of("Broccoli", 34, 3, 7, 0,
                        FoodCategory.VEGETABLE,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Spinach", 23, 3, 4, 0,
                        FoodCategory.VEGETABLE,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Tomato", 18, 1, 4, 0,
                        FoodCategory.VEGETABLE,
                        Set.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)),

                Food.of("Carrot", 41, 1, 10, 0,
                        FoodCategory.VEGETABLE,
                        Set.of(MealType.LUNCH, MealType.DINNER, MealType.SNACK)),

                Food.of("Zucchini", 17, 1, 3, 0,
                        FoodCategory.VEGETABLE,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Bell Pepper", 31, 1, 6, 0,
                        FoodCategory.VEGETABLE,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                // ── FRUITS ────────────────────────────────────────────────

                // Frutas con UNIT — peso promedio por unidad
                Food.ofUnit("Apple", 52, 0, 14, 0,
                        FoodCategory.FRUIT,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING, MealType.DINNER),
                        150.0),

                Food.ofUnit("Banana", 89, 1, 23, 0,
                        FoodCategory.FRUIT,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING, MealType.DINNER),
                        120.0),

                Food.ofUnit("Orange", 47, 1, 12, 0,
                        FoodCategory.FRUIT,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING, MealType.DINNER),
                        130.0),

                Food.ofUnit("Pear", 57, 0, 15, 0,
                        FoodCategory.FRUIT,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING, MealType.DINNER),
                        140.0),

                // ── FATS ──────────────────────────────────────────────────

                Food.of("Avocado", 160, 2, 9, 15,
                        FoodCategory.FAT,
                        Set.of(MealType.BREAKFAST, MealType.LUNCH)),

                Food.of("Mixed Nuts", 607, 20, 21, 54,
                        FoodCategory.FAT,
                        Set.of(MealType.SNACK)),

                Food.of("Peanut Butter", 588, 25, 20, 50,
                        FoodCategory.FAT,
                        Set.of(MealType.BREAKFAST, MealType.SNACK)),

                // Aceite con MILLILITER — densidad 0.92
                Food.ofLiquid("Olive Oil", 884, 0, 0, 100,
                        FoodCategory.FAT,
                        Set.of(MealType.LUNCH, MealType.DINNER),
                        0.92),

                // ── SWEETS ────────────────────────────────────────────────

                Food.of("Honey Granola", 471, 10, 64, 20,
                        FoodCategory.SWEET,
                        Set.of(MealType.BREAKFAST, MealType.LUNCH, MealType.SNACK)),

                Food.of("Dark Chocolate", 546, 5, 60, 31,
                        FoodCategory.SWEET,
                        Set.of(MealType.LUNCH, MealType.DINNER, MealType.SNACK)),

                Food.of("Rice Pudding", 130, 3, 22, 4,
                        FoodCategory.SWEET,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                // ── BEVERAGES ─────────────────────────────────────────────

                Food.ofLiquid("Water", 0, 0, 0, 0,
                        FoodCategory.BEVERAGE,
                        Set.of(MealType.BREAKFAST, MealType.LUNCH,
                                MealType.SNACK, MealType.DINNER, MealType.MID_MORNING),
                        1.0),

                Food.ofLiquid("Green Tea", 1, 0, 0, 0,
                        FoodCategory.BEVERAGE,
                        Set.of(MealType.BREAKFAST, MealType.SNACK, MealType.MID_MORNING),
                        1.0),

                Food.ofLiquid("Orange Juice", 45, 1, 10, 0,
                        FoodCategory.BEVERAGE,
                        Set.of(MealType.BREAKFAST),
                        1.04),

                Food.ofLiquid("Soy Milk", 54, 3, 6, 2,
                        FoodCategory.BEVERAGE,
                        Set.of(MealType.BREAKFAST, MealType.SNACK),
                        1.02)
        ));

        applyFoodTags(foodRepository);
    }

    private void applyFoodTags(FoodRepository repo) {

        repo.findByName("Chicken Breast").ifPresent(f -> {
            f.addTag(FoodTag.MEAT); repo.save(f); });

        repo.findByName("Salmon").ifPresent(f -> {
            f.addTag(FoodTag.FISH); repo.save(f); });

        repo.findByName("Ground Beef").ifPresent(f -> {
            f.addTag(FoodTag.MEAT); repo.save(f); });

        repo.findByName("Scrambled Eggs").ifPresent(f -> {
            f.addTag(FoodTag.EGG); repo.save(f); });

        repo.findByName("Whole Milk Yogurt").ifPresent(f -> {
            f.addTag(FoodTag.LACTOSE); repo.save(f); });

        repo.findByName("Cottage Cheese").ifPresent(f -> {
            f.addTag(FoodTag.LACTOSE); repo.save(f); });

        repo.findByName("Whole Milk").ifPresent(f -> {
            f.addTag(FoodTag.LACTOSE); repo.save(f); });

        repo.findByName("Oatmeal").ifPresent(f -> {
            f.addTag(FoodTag.GLUTEN); repo.save(f); });

        repo.findByName("Whole Wheat Pasta").ifPresent(f -> {
            f.addTag(FoodTag.GLUTEN); repo.save(f); });

        repo.findByName("Whole Grain Bread").ifPresent(f -> {
            f.addTag(FoodTag.GLUTEN); repo.save(f); });

        repo.findByName("Tofu").ifPresent(f -> {
            f.addTag(FoodTag.SOY); repo.save(f); });

        repo.findByName("Soy Milk").ifPresent(f -> {
            f.addTag(FoodTag.SOY); repo.save(f); });

        repo.findByName("Mixed Nuts").ifPresent(f -> {
            f.addTag(FoodTag.NUTS); repo.save(f); });

        repo.findByName("Peanut Butter").ifPresent(f -> {
            f.addTag(FoodTag.NUTS); repo.save(f); });

        repo.findByName("Honey Granola").ifPresent(f -> {
            f.addTag(FoodTag.HONEY);
            f.addTag(FoodTag.GLUTEN);
            repo.save(f);
        });
    }
}