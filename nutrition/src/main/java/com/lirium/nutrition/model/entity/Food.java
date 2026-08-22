package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Food catalog entity with nutritional values per 100g.
 * Values are validated on creation and tags are stored as value types
 * using an ElementCollection.
 */
@Entity
@Table(name = "foods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Food extends DateAuditable{

    @Id
    @SequenceGenerator(
            name = "food_seq",
            sequenceName = "food_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "food_seq")
    private Long id;

    @Column(nullable = false, unique = true, length = 120, name = "name")
    private String name;

    @Column(nullable = false, name = "active")
    private boolean active = true;

    @Column(name = "density")
    private Double density;

    @Column(name = "unit_weight")
    private Double unitWeight;

    @Column(nullable = false, name = "calories_per_100g")
    private Integer caloriesPer100g;

    @Column(nullable = false, name = "protein_per_100g")
    private Integer proteinPer100g;

    @Column(nullable = false, name = "carbs_per_100g")
    private Integer carbsPer100g;

    @Column(nullable = false, name = "fat_per_100g")
    private Integer fatPer100g;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FoodCategory category;

    @Column(name = "min_serving_grams")
    private Double minServingGrams;

    @Column(name = "max_serving_grams")
    private Double maxServingGrams;

    @Column(name = "default_unit")
    private MeasureUnit defaultUnit;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "food_suitable_for",
            joinColumns = @JoinColumn(name = "food_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type")
    private Set<MealType> suitableFor = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "food_tags",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(
                name = "uk_food_tags_food_tag",
                columnNames = {"food_id", "tag"}
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Set<FoodTag> foodTags = new HashSet<>();

    private Food(
            String name,
            Integer caloriesPer100g,
            Integer proteinPer100g,
            Integer carbsPer100g,
            Integer fatPer100g,
            FoodCategory category
    ) {
        name = Objects.requireNonNull(name, "Name cannot be null");
        if (name.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
        this.name = name;
        this.caloriesPer100g = requireRange(caloriesPer100g, 0, 1000, "Calories");
        this.proteinPer100g  = requireRange(proteinPer100g, 0, 100, "Protein");
        this.carbsPer100g    = requireRange(carbsPer100g, 0, 100, "Carbs");
        this.fatPer100g      = requireRange(fatPer100g, 0, 100, "Fat");
        this.category        = Objects.requireNonNull(category, "Category cannot be null");
    }

    public static Food of(
            String name,
            Integer caloriesPer100g,
            Integer proteinPer100g,
            Integer carbsPer100g,
            Integer fatPer100g,
            FoodCategory category,
            Set<MealType> suitableFor
    ) {
        Food food = new Food(name, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g, category);
        if (suitableFor != null) food.suitableFor.addAll(suitableFor);
        food.defaultUnit = MeasureUnit.GRAM;
        return food;
    }

    public static Food ofLiquid(
            String name,
            Integer caloriesPer100g,
            Integer proteinPer100g,
            Integer carbsPer100g,
            Integer fatPer100g,
            FoodCategory category,
            Set<MealType> suitableFor,
            Double density) {
        Food food = new Food(name, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g, category);
        if (suitableFor != null) food.suitableFor.addAll(suitableFor);
        food.defaultUnit = MeasureUnit.MILLILITER;
        food.setDensity(density);
        return food;
    }

    public static Food ofUnit(
            String name,
            Integer caloriesPer100g,
            Integer proteinPer100g,
            Integer carbsPer100g,
            Integer fatPer100g,
            FoodCategory category,
            Set<MealType> suitableFor,
            Double unitWeight) {
        Food food = new Food(name, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g, category);
        if (suitableFor != null) food.suitableFor.addAll(suitableFor);
        food.defaultUnit = MeasureUnit.UNIT;
        food.setUnitWeight(unitWeight);
        return food;
    }

    public static Food of(
            String name,
            Integer caloriesPer100g,
            Integer proteinPer100g,
            Integer carbsPer100g,
            Integer fatPer100g,
            FoodCategory category,
            Set<MealType> suitableFor,
            Double minServingGrams,
            Double maxServingGrams
    ) {
        Food food = of(name, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g, category, suitableFor);
        food.changeServingLimits(minServingGrams, maxServingGrams);
        return food;
    }

    public void changeServingLimits(Double minGrams, Double maxGrams) {
        if (minGrams != null && minGrams <= 0) {
            throw new IllegalArgumentException("Min serving grams must be positive");
        }
        if (maxGrams != null && maxGrams <= 0) {
            throw new IllegalArgumentException("Max serving grams must be positive");
        }
        if (minGrams != null && maxGrams != null && minGrams > maxGrams) {
            throw new IllegalArgumentException("Min serving grams cannot be greater than max serving grams");
        }
        this.minServingGrams = minGrams;
        this.maxServingGrams = maxGrams;
    }

    private static Integer requireRange(Integer value, int min, int max, String field) {
        Objects.requireNonNull(value, field + " cannot be null");
        if (value < min || value > max)
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max);
        return value;
    }

    public void setDensity(Double density) {
        if (density != null && density <= 0)
            throw new IllegalArgumentException("Density must be positive");
        this.density = density;
    }

    public void setUnitWeight(Double unitWeight) {
        if (unitWeight != null && unitWeight <= 0)
            throw new IllegalArgumentException("Unit weight must be positive");
        this.unitWeight = unitWeight;
    }

    public Double toGrams(Double quantity, MeasureUnit unit) {
        return switch (unit) {
            case GRAM -> quantity;
            case MILLILITER -> {
                if (density == null)
                    throw new IllegalStateException("Food '" + name + "' has no density defined");
                yield quantity * density;
            }
            case UNIT -> {
                if (unitWeight == null)
                    throw new IllegalStateException("Food '" + name + "' has no unit weight defined");
                yield quantity * unitWeight;
            }
        };
    }

    public Set<MealType> getSuitableFor() {
        return Collections.unmodifiableSet(suitableFor);
    }

    public void addSuitableFor(MealType mealType) {
        Objects.requireNonNull(mealType, "MealType cannot be null");
        suitableFor.add(mealType);
    }

    public void removeSuitableFor(MealType mealType) {
        Objects.requireNonNull(mealType, "MealType cannot be null");
        suitableFor.remove(mealType);
    }

    public void changeName(String name) {
        name = Objects.requireNonNull(name, "Name cannot be null");
        if (name.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
        this.name = name;
    }

    public void changeCalories(Integer calories) {
        this.caloriesPer100g = requireRange(calories, 0, 1000, "Calories");
    }

    public void changeProtein(Integer protein) {
        this.proteinPer100g = requireRange(protein, 0, 100, "Protein");
    }

    public void changeCarbs(Integer carbs) {
        this.carbsPer100g = requireRange(carbs, 0, 100, "Carbs");
    }

    public void changeFat(Integer fat) {
        this.fatPer100g = requireRange(fat, 0, 100, "Fat");
    }

    public void replaceTags(Set<FoodTag> tags) {
        foodTags.clear();
        if (tags != null) tags.forEach(this::addTag);
    }

    public void clearTags() {
        foodTags.clear();
    }

    public Set<FoodTag> getFoodTags() {
        return Collections.unmodifiableSet(foodTags);
    }

    public void addTag(FoodTag tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        foodTags.add(tag);
    }

    public void removeTag(FoodTag tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        foodTags.remove(tag);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}