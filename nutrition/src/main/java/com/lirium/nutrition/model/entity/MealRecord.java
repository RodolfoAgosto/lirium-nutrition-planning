package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate root representing a consumed meal.
 * Contains food portions, meal type, and consumption time.
 * A meal may originate from a plan or be spontaneous (overridden).
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Table(name = "meal_records")
public class MealRecord extends DateAuditable {

    @Id
    @SequenceGenerator(
            name = "meal_seq",
            sequenceName = "meal_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meal_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType type;

    @OneToMany(mappedBy = "meal",cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FoodPortionRecord> foods = new ArrayList<>();

    @Column(name = "overridden", nullable = false)
    private boolean overridden;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "eaten_at", nullable = false)
    private LocalDateTime eatenAt;

    // Creates a record linked to a plan. Initially, 'overriden' is false as it follows the prescription.
    private MealRecord(PlanMeal planMeal, LocalDateTime eatenAt, DailyRecord dailyRecord){
        System.out.println(">>> PlanMeal ID: " + planMeal.getId() + " - Cantidad de alimentos en getFoods(): " + (planMeal.getFoods() != null ? planMeal.getFoods().size() : "NULL"));
        Objects.requireNonNull(planMeal, "PlanMeal must be provided");
        Objects.requireNonNull(eatenAt, "Date must be provided.");
        Objects.requireNonNull(dailyRecord, "DailyRecord must be provided");
        Objects.requireNonNull(planMeal.getFoods(), "PlanMeal foods null");
        Objects.requireNonNull(planMeal.getType(), "Meal type null");
        if (eatenAt.toLocalDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Meal cannot be in the future");
        this.type = planMeal.getType();
        this.eatenAt = eatenAt;
        this.dailyRecord = dailyRecord;
        this.overridden = false;

        planMeal.getFoods().forEach(food -> {
            FoodPortionRecord portion = FoodPortionRecord.of(
                    this,
                    food.getFood(),
                    food.getQuantity(),
                    food.getMeasureUnit()
            );
            this.foods.add(portion);
        });

    }


    // Creates a spontaneous record. Marked as 'modified' because it deviates from the original plan.
    private MealRecord(MealType mealType, LocalDateTime eatenAt, DailyRecord dailyRecord){
        Objects.requireNonNull(mealType, "Meal type must be provided");
        Objects.requireNonNull(eatenAt, "DateTime must be provided.");
        Objects.requireNonNull(dailyRecord, "DailyRecord must be provided");
        if (eatenAt.toLocalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Meal cannot be in the future");
        }
        this.type = mealType;
        this.eatenAt = eatenAt;
        this.dailyRecord = dailyRecord;
        this.overridden = false;
    }

    public static MealRecord of(MealType mealType, LocalDateTime eatenAt, DailyRecord dailyRecord){
        return new MealRecord(mealType, eatenAt, dailyRecord);
    }

    public static MealRecord fromPlan(PlanMeal planMeal, LocalDateTime eatenAt, DailyRecord dailyRecord){
        return new MealRecord(planMeal, eatenAt, dailyRecord);
    }

    public void addFoodPortion(Food food, Double quantity , MeasureUnit unit) {
        Objects.requireNonNull(food, "Food must not be null");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        Objects.requireNonNull(unit, "Measure unit must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        // Busca si ya existe el mismo alimento con la misma unidad para acumular la cantidad
        Optional<FoodPortionRecord> existingPortion = foods.stream()
                .filter(fp -> fp.getFood().equals(food) && fp.getUnit() == unit)
                .findFirst();
        this.overridden = true;

        existingPortion.ifPresent(foodPortionRecord -> foods.remove(foodPortionRecord));
        foods.add(FoodPortionRecord.of(this, food, quantity, unit));

    }

    public void removeFoodPortion(FoodPortionRecord foodPortionRecord) {

        Objects.requireNonNull(foodPortionRecord);
        this.overridden = true;
        foods.remove(foodPortionRecord);

    }

    public void clearFoods() {
        foods.clear();
    }

    public List<FoodPortionRecord> getFoodPortions() {
        return Collections.unmodifiableList(foods);
    }

    public void markAsOverridden() {
        this.overridden = true;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_record_id", nullable = false)
    private DailyRecord dailyRecord;

}