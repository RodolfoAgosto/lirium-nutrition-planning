package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer caloriesPer100g;

    @Column(nullable = false)
    private Integer proteinPer100g;

    @Column(nullable = false)
    private Integer carbsPer100g;

    @Column(nullable = false)
    private Integer fatPer100g;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_tags", joinColumns = @JoinColumn(name = "food_id"), uniqueConstraints = @UniqueConstraint(columnNames = {"food_id","tag"}))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Set<FoodTag> foodTags = new HashSet<>();

    private Food(String name, Integer caloriesPer100g, Integer proteinPer100g, Integer carbsPer100g, Integer fatPer100g){

        name = Objects.requireNonNull(name, "Name cannot be null");
        if (name.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
        this.name = name;
        this.caloriesPer100g = requireRange(caloriesPer100g, 0, 1000, "Calories");
        this.proteinPer100g  = requireRange(proteinPer100g, 0, 100, "Protein");
        this.carbsPer100g    = requireRange(carbsPer100g, 0, 100, "Carbs");
        this.fatPer100g      = requireRange(fatPer100g, 0, 100, "Fat");

    }

    private static Integer requireRange(Integer value, int min, int max, String field) {
        Objects.requireNonNull(value, field + " cannot be null");
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " value must be between " + min + " and " + max);
        }
        return value;
    }

    public static Food of(String name, Integer caloriesPer100g, Integer proteinPer100g, Integer carbsPer100g, Integer fatPer100g){
        return new Food(name, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g);
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

}
