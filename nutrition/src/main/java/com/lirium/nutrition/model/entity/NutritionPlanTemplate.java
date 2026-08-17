package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.exception.UnprocessableEntityException;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a reusable nutrition plan template.
 * Defines goal type, macronutrient distribution
 * and optional food tag exclusions.
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "nutrition_plan_template")
public class NutritionPlanTemplate extends Auditable {

    @Id
    @SequenceGenerator(
            name = "nutrition_plan_template_seq",
            sequenceName = "nutrition_plan_template_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nutrition_plan_template_seq")
    private Long id;

    @Column(nullable = false, unique = true, name = "name")
    private String name;

    @Column(nullable = false, name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_goal")
    private GoalType targetGoal;

    @Column(name = "protein_percentage", nullable = false)
    private int proteinPercentage;

    @Column(name = "carb_percentage", nullable = false)
    private int carbPercentage;

    @Column(name = "fat_percentage", nullable = false)
    private int fatPercentage;

    public Set<FoodTag> getExcludedTags() {
        return Set.copyOf(excludedTags);
    }

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "nutrition_plan_template_excluded_tags",
            joinColumns = @JoinColumn(name = "nutrition_plan_template_id")
    )
    @Column(name = "food_tag")
    private final Set<FoodTag> excludedTags = new HashSet<>();


    public static NutritionPlanTemplate of(
            String name,
            String description,
            GoalType targetGoal,
            int proteinPercentage,
            int carbPercentage,
            int fatPercentage,
            Set<FoodTag> excludedTags
    ) {

        requireText(name, "Name required");
        requireText(description, "Description required");
        Objects.requireNonNull(targetGoal, "Goal required");

        validateMacros(proteinPercentage, carbPercentage, fatPercentage);

        NutritionPlanTemplate template = new NutritionPlanTemplate();
        template.name = name;
        template.description = description;
        template.targetGoal = targetGoal;
        template.proteinPercentage = proteinPercentage;
        template.carbPercentage = carbPercentage;
        template.fatPercentage = fatPercentage;

        if (excludedTags != null)
            template.excludedTags.addAll(excludedTags);

        return template;
    }

    public void update(
            String name,
            String description,
            GoalType targetGoal,
            Set<FoodTag> excludedTags
    ) {
        if (name != null) requireText(name, "Name required");
        if (description != null) requireText(description, "Description required");

        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (targetGoal != null) this.targetGoal = targetGoal;
        if (excludedTags != null) {
            this.excludedTags.clear();
            this.excludedTags.addAll(excludedTags);
        }
    }

    public void updateMacros(int protein, int carb, int fat) {

        validateMacros(protein, carb, fat);

        this.proteinPercentage = protein;
        this.carbPercentage = carb;
        this.fatPercentage = fat;
    }

    private static void requireText(String s, String msg) {
        if (s == null || s.isBlank())
            throw new IllegalArgumentException(msg);
    }

    private static void validateMacros(int protein, int carb, int fat) {
        if (protein < 0 || carb < 0 || fat < 0) {
            throw new IllegalArgumentException("Percentages cannot be negative");
        }
        if (protein + carb + fat != 100) {
            throw new UnprocessableEntityException(
                    String.format("Macro percentages must sum to 100. Current sum: %d", (protein + carb + fat))
            );
        }
    }

}