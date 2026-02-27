package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.valueobject.Grams;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregate root representing a consumed meal.
 * Contains food portions, meal type, and consumption time.
 * A meal may originate from a plan or be spontaneous (overridden).
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class MealRecord {

    @Id
    @SequenceGenerator(
            name = "meal_seq",
            sequenceName = "meal_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meal_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    private MealType type;

    @OneToMany(mappedBy = "meal",cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FoodPortionRecord> foods = new ArrayList<>();

    private boolean overridden;

    @Column(length = 500)
    private String notes;

    private LocalDateTime eatenAt;

    // Creates a record linked to a plan. Initially, 'overriden' is false as it follows the prescription.
    private MealRecord(PlanMeal planMeal, LocalDateTime eatenAt){
        Objects.requireNonNull(planMeal, "PlanMeal must be provided");
        Objects.requireNonNull(eatenAt, "Date must be provided.");
        Objects.requireNonNull(planMeal.getFoods(), "PlanMeal foods null");
        Objects.requireNonNull(planMeal.getType(), "Meal type null");
        if (eatenAt.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Meal cannot be in the future");
        this.type = planMeal.getType();
        this.eatenAt = eatenAt;
        this.foods = planMeal.getFoods().stream()
                .map(food -> FoodPortionRecord.of(this, food.getFood(), food.grams))
                .collect(Collectors.toList());
    }

    // Creates a spontaneous record. Marked as 'modified' because it deviates from the original plan.
    private MealRecord(MealType mealType, LocalDateTime eatenAt){
        Objects.requireNonNull(mealType, "Meal type must be provided");
        Objects.requireNonNull(eatenAt, "DateTime must be provided.");
        if (eatenAt.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Meal cannot be in the future");
        this.type = mealType;
        this.overridden = true;
        this.eatenAt = eatenAt;
    }

    public static MealRecord of(MealType mealType, LocalDateTime eatenAt){
        return new MealRecord(mealType, eatenAt);
    }

    public static MealRecord fromPlan(PlanMeal planMeal, LocalDateTime eatenAt){
        return new MealRecord(planMeal, eatenAt);
    }
    public void addFoodPortion(Food food, Grams grams) {
        Objects.requireNonNull(food);
        Objects.requireNonNull(grams);
        FoodPortionRecord foodPortionRecord = FoodPortionRecord.of(this, food, grams);
        boolean exists = foods.stream()
                .anyMatch(fp ->
                        fp.getFood().equals(food) &&
                                fp.getGrams().equals(grams)
                );

        if (!exists) {
            foods.add(foodPortionRecord);
        }
    }

    public void removeFoodPortion(FoodPortionRecord foodPortionRecord) {

        Objects.requireNonNull(foodPortionRecord);
        foods.remove(foodPortionRecord);

    }

    public void clearFoods() {
        foods.clear();
    }

    public List<FoodPortionRecord> getFoodPortions() {
        return Collections.unmodifiableList(foods);
    }

    public void markAsOverridden(String reason) {
        Objects.requireNonNull(reason);
        requireText(reason, "Reason required");
        this.notes = reason;
        this.overridden = true;
    }

    public void updateNotes(String notes) {
        this.requireText(notes, "Notes required");
        this.notes = notes;
    }

    public void clearOverride(){
        this.overridden = false;
        this.notes = null;
    }

    private static void requireText(String s, String msg) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(msg);
    }

}