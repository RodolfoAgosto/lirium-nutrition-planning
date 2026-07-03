package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FoodMapperTest {

    @Test
    void shouldMapEntityToResponse() {

        Food food = Food.of(
                "Milk",
                60,
                3,
                5,
                3,
                FoodCategory.DAIRY,
                Set.of(MealType.BREAKFAST)
        );

        food.addTag(FoodTag.LACTOSE);

        FoodResponseDTO dto = FoodMapper.toResponse(food);

        assertThat(dto.id()).isNull();
        assertThat(dto.name()).isEqualTo("Milk");
        assertThat(dto.caloriesPer100g()).isEqualTo(60);
        assertThat(dto.proteinPer100g()).isEqualTo(3);
        assertThat(dto.carbsPer100g()).isEqualTo(5);
        assertThat(dto.fatPer100g()).isEqualTo(3);
        assertThat(dto.foodCategory()).isEqualTo(FoodCategory.DAIRY);
        assertThat(dto.suitableFor()).containsExactly(MealType.BREAKFAST);
        assertThat(dto.tags()).containsExactly(FoodTag.LACTOSE);
    }

    @Test
    void shouldMapEntityToSummary() {

        Food food = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of()
        );

        FoodSummaryDTO dto = FoodMapper.toSummary(food);

        assertThat(dto.id()).isNull();
        assertThat(dto.name()).isEqualTo("Apple");
    }

    @Test
    void shouldMapCreateDtoToEntity() {

        FoodCreateRequestDTO dto = new FoodCreateRequestDTO(
                "Fish",
                120,
                22,
                0,
                3,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH, MealType.DINNER),
                Set.of("FISH")
        );

        Food entity = FoodMapper.toEntity(dto);

        assertThat(entity.getName()).isEqualTo("Fish");
        assertThat(entity.getCaloriesPer100g()).isEqualTo(120);
        assertThat(entity.getProteinPer100g()).isEqualTo(22);
        assertThat(entity.getCarbsPer100g()).isEqualTo(0);
        assertThat(entity.getFatPer100g()).isEqualTo(3);
        assertThat(entity.getCategory()).isEqualTo(FoodCategory.PROTEIN);
        assertThat(entity.getSuitableFor())
                .containsExactlyInAnyOrder(MealType.LUNCH, MealType.DINNER);
        assertThat(entity.getFoodTags())
                .containsExactly(FoodTag.FISH);
    }

    @Test
    void shouldMapCreateDtoWithoutTags() {

        FoodCreateRequestDTO dto = new FoodCreateRequestDTO(
                "Rice",
                130,
                3,
                28,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH),
                null
        );

        Food entity = FoodMapper.toEntity(dto);

        assertThat(entity.getFoodTags()).isEmpty();
    }

    @Test
    void shouldUpdateEntity() {

        Food entity = Food.of(
                "Milk",
                60,
                3,
                5,
                3,
                FoodCategory.DAIRY,
                Set.of(MealType.BREAKFAST)
        );

        entity.addTag(FoodTag.LACTOSE);

        FoodUpdateRequestDTO dto = new FoodUpdateRequestDTO(
                "Soy Milk",
                45,
                4,
                2,
                2,
                Set.of("SOY")
        );

        FoodMapper.updateEntity(entity, dto);

        assertThat(entity.getName()).isEqualTo("Soy Milk");
        assertThat(entity.getCaloriesPer100g()).isEqualTo(45);
        assertThat(entity.getProteinPer100g()).isEqualTo(4);
        assertThat(entity.getCarbsPer100g()).isEqualTo(2);
        assertThat(entity.getFatPer100g()).isEqualTo(2);
        assertThat(entity.getFoodTags()).containsExactly(FoodTag.SOY);
    }

    @Test
    void shouldNotUpdateNullFields() {

        Food entity = Food.of(
                "Egg",
                155,
                13,
                1,
                11,
                FoodCategory.PROTEIN,
                Set.of(MealType.BREAKFAST)
        );

        entity.addTag(FoodTag.EGG);

        FoodUpdateRequestDTO dto = new FoodUpdateRequestDTO(
                null,
                null,
                null,
                null,
                null,
                null
        );

        FoodMapper.updateEntity(entity, dto);

        assertThat(entity.getName()).isEqualTo("Egg");
        assertThat(entity.getCaloriesPer100g()).isEqualTo(155);
        assertThat(entity.getProteinPer100g()).isEqualTo(13);
        assertThat(entity.getCarbsPer100g()).isEqualTo(1);
        assertThat(entity.getFatPer100g()).isEqualTo(11);
        assertThat(entity.getFoodTags()).containsExactly(FoodTag.EGG);
    }
}