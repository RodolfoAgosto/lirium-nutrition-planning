package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * Entity representing a reusable nutrition plan template.
 * Defines goal type, macronutrient distribution
 * and optional food tag exclusions.
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class NutritionPlanTemplate {

    @Id
    @SequenceGenerator(
            name = "nutrition_plan_template_seq",
            sequenceName = "nutrition_plan_template_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nutrition_plan_template_seq")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private GoalType targetGoal;

    private int proteinPercentage;
    private int carbPercentage;
    private int fatPercentage;

    @ElementCollection(targetClass = FoodTag.class)
    @Enumerated(EnumType.STRING)
    private Set<FoodTag> excludedTags = new HashSet<>();

    public Set<FoodTag> getExcludedTags() {
        return Set.copyOf(excludedTags);
    }

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

        if (proteinPercentage < 0 || carbPercentage < 0 || fatPercentage < 0)
            throw new IllegalArgumentException("Percentages cannot be negative");

        int sum = proteinPercentage + carbPercentage + fatPercentage;
        if (sum != 100)
            throw new IllegalArgumentException("Macro percentages must sum 100");

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

    private static void requireText(String s, String msg) {
        if (s == null || s.isBlank())
            throw new IllegalArgumentException(msg);
    }

}