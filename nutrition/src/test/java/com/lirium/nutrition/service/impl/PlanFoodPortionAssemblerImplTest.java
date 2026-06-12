package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.*;
import com.lirium.nutrition.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanFoodPortionAssemblerImplTest {

    @Mock
    private FoodRepository foodRepository;

    @Test
    void shouldAssembleSingleMealWithSingleFood() {

        // Given
        PlanFoodPortionAssemblerImpl assembler =
                new PlanFoodPortionAssemblerImpl(foodRepository);

        PlanMeal meal = mock(PlanMeal.class);
        PatientProfile patient = mock(PatientProfile.class);

        Calories calories = mock(Calories.class);
        Fat fat = mock(Fat.class);
        Carbs carbs = mock(Carbs.class);
        Protein protein = mock(Protein.class);

        Food fruit = mock(Food.class);
        Food dairy = mock(Food.class);

        when(meal.getType()).thenReturn(MealType.MID_MORNING);

        when(calories.amount()).thenReturn(1000);
        when(carbs.amount()).thenReturn(100);
        when(fat.amount()).thenReturn(50);
        when(protein.grams()).thenReturn(80);

        when(patient.getRestrictions()).thenReturn(Set.of());

        lenient().when(fruit.getCategory()).thenReturn(FoodCategory.FRUIT);
        lenient().when(dairy.getCategory()).thenReturn(FoodCategory.DAIRY);

        lenient().when(fruit.getDefaultUnit()).thenReturn(MeasureUnit.UNIT);
        lenient().when(dairy.getDefaultUnit()).thenReturn(MeasureUnit.UNIT);

        lenient().when(fruit.getCaloriesPer100g()).thenReturn(50);
        lenient().when(dairy.getCaloriesPer100g()).thenReturn(50);

        lenient().when(fruit.getCarbsPer100g()).thenReturn(10);
        lenient().when(dairy.getCarbsPer100g()).thenReturn(10);

        lenient().when(fruit.getFatPer100g()).thenReturn(1);
        lenient().when(dairy.getFatPer100g()).thenReturn(1);

        lenient().when(fruit.getProteinPer100g()).thenReturn(1);
        lenient().when(dairy.getProteinPer100g()).thenReturn(1);

        lenient().when(fruit.toGrams(anyDouble(), any()))
                .thenReturn(100.0);

        lenient().when(dairy.toGrams(anyDouble(), any()))
                .thenReturn(100.0);

        when(foodRepository.findSuitableFoods(
                eq(MealType.MID_MORNING),
                anySet()))
                .thenReturn(new ArrayList<>(List.of(fruit, dairy)));

        // When
        assembler.assemble(
                meal,
                patient,
                calories,
                fat,
                carbs,
                protein
        );

        // Then
        verify(foodRepository)
                .findSuitableFoods(eq(MealType.MID_MORNING), anySet());

        verify(meal, atLeastOnce())
                .addFoodPortion(any());
    }


    @Test
    void shouldAssembleMidMorningMeal() {

        PlanMeal meal = mock(PlanMeal.class);
        PatientProfile patient = mock(PatientProfile.class);

        when(meal.getType()).thenReturn(MealType.MID_MORNING);
        when(patient.getRestrictions()).thenReturn(Set.of());

        Food fruit = Food.ofUnit(
                "Banana",
                90,
                1,
                20,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.MID_MORNING),
                120.0
        );

        Food dairy = Food.of(
                "Yogurt",
                60,
                5,
                6,
                2,
                FoodCategory.DAIRY,
                Set.of(MealType.MID_MORNING)
        );

        when(foodRepository.findSuitableFoods(eq(MealType.MID_MORNING), anySet()))
                .thenReturn(new ArrayList<>(List.of(fruit, dairy)));

        PlanFoodPortionAssemblerImpl assembler =
                new PlanFoodPortionAssemblerImpl(foodRepository);

        assembler.assemble(
                meal,
                patient,
                new Calories(300),
                new Fat(10),
                new Carbs(30),
                new Protein(20)
        );

        verify(meal).addFoodPortion(any());
    }

    @Test
    void shouldAssembleLunchWithAllCategories() {

        PlanMeal meal = mock(PlanMeal.class);
        PatientProfile patient = mock(PatientProfile.class);

        when(meal.getType()).thenReturn(MealType.LUNCH);
        when(patient.getRestrictions()).thenReturn(Set.of());

        Food protein = Food.of(
                "Chicken", 150, 30, 0, 5,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH));

        Food carb = Food.of(
                "Rice", 120, 2, 28, 1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH));

        Food vegetable = Food.of(
                "Tomato", 20, 1, 4, 0,
                FoodCategory.VEGETABLE,
                Set.of(MealType.LUNCH));

        Food sweet = Food.of(
                "Cookie", 400, 4, 70, 10,
                FoodCategory.SWEET,
                Set.of(MealType.LUNCH));

        Food beverage = Food.ofLiquid(
                "Juice", 40, 0, 10, 0,
                FoodCategory.BEVERAGE,
                Set.of(MealType.LUNCH),
                1.0
        );

        when(foodRepository.findSuitableFoods(eq(MealType.LUNCH), anySet()))
                .thenReturn(new ArrayList<>(List.of(
                        protein,
                        carb,
                        vegetable,
                        sweet,
                        beverage
                )));

        PlanFoodPortionAssemblerImpl assembler =
                new PlanFoodPortionAssemblerImpl(foodRepository);

        assembler.assemble(
                meal,
                patient,
                new Calories(700),
                new Fat(20),
                new Carbs(80),
                new Protein(50)
        );

        verify(meal, times(5))
                .addFoodPortion(any(PlanFoodPortion.class));
    }

    @Test
    void shouldMergeRestrictionTagsAndAdditionalExcludedTags() {

        PlanMeal meal = mock(PlanMeal.class);
        PatientProfile patient = mock(PatientProfile.class);

        Restriction restriction = Restriction.builder()
                .excludedTags(Set.of(FoodTag.GLUTEN))
                .build();

        when(meal.getType()).thenReturn(MealType.LUNCH);
        when(patient.getRestrictions())
                .thenReturn(Set.of(restriction));

        Food protein = Food.of(
                "Chicken",
                150,
                30,
                0,
                5,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH)
        );

        Food carb = Food.of(
                "Rice",
                120,
                2,
                28,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        Food vegetable = Food.of(
                "Tomato",
                20,
                1,
                4,
                0,
                FoodCategory.VEGETABLE,
                Set.of(MealType.LUNCH)
        );

        Food sweet = Food.of(
                "Cookie",
                400,
                4,
                70,
                10,
                FoodCategory.SWEET,
                Set.of(MealType.LUNCH)
        );

        Food beverage = Food.ofLiquid(
                "Juice",
                40,
                0,
                10,
                0,
                FoodCategory.BEVERAGE,
                Set.of(MealType.LUNCH),
                1.0
        );

        when(foodRepository.findSuitableFoods(eq(MealType.LUNCH), anySet()))
                .thenReturn(new ArrayList<>(List.of(
                        protein,
                        carb,
                        vegetable,
                        sweet,
                        beverage
                )));

        PlanFoodPortionAssemblerImpl assembler =
                new PlanFoodPortionAssemblerImpl(foodRepository);

        assembler.assemble(
                meal,
                patient,
                Set.of(FoodTag.HONEY),
                new Calories(700),
                new Fat(20),
                new Carbs(80),
                new Protein(50)
        );

        ArgumentCaptor<Set<FoodTag>> captor =
                ArgumentCaptor.forClass(Set.class);

        verify(foodRepository)
                .findSuitableFoods(eq(MealType.LUNCH), captor.capture());

        Set<FoodTag> tags = captor.getValue();

        assertTrue(tags.contains(FoodTag.GLUTEN));
        assertTrue(tags.contains(FoodTag.HONEY));
    }

}