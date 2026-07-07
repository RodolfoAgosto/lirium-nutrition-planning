package com.lirium.nutrition.testdata;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.repository.DailyRecordRepository;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class DailyRecordTestDataFactory {

    private final DailyRecordRepository dailyRecordRepository;
    private final FoodRepository foodRepository;
    private final NutritionPlanTestDataFactory nutritionPlanTestDataFactory;
    private final NutritionPlanRepository nutritionPlanRepository;


    @Transactional
    public DailyRecord createTodayDailyRecord(PatientProfile patient) {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                200,
                70,
                patient
        );

        plan.activate(LocalDate.now());

        nutritionPlanRepository.save(plan);


        DailyRecord dailyRecord = DailyRecord.of(
                patient,
                LocalDate.now()
        );

        return dailyRecordRepository.save(dailyRecord);
    }

    @Transactional
    public DailyRecord createDailyRecord(
            PatientProfile patient,
            LocalDate date
    ) {

        DailyRecord dailyRecord = DailyRecord.of(
                patient,
                date
        );

        createMeals(dailyRecord);

        return dailyRecordRepository.save(dailyRecord);
    }


    @Transactional
    public MealRecord createMealWithFood(
            DailyRecord dailyRecord
    ) {

        MealRecord meal = MealRecord.of(
                MealType.LUNCH,
                LocalDateTime.now().withHour(13),
                dailyRecord
        );

        meal.addFoodPortion(
                chicken(),
                150.0,
                MeasureUnit.GRAM
        );

        dailyRecord.addMeal(meal);

        dailyRecordRepository.save(dailyRecord);

        return meal;
    }


    @Transactional
    public MealRecord createEmptyMeal(
            DailyRecord dailyRecord
    ) {

        MealRecord meal = MealRecord.of(
                MealType.BREAKFAST,
                LocalDateTime.now().withHour(8),
                dailyRecord
        );

        dailyRecord.addMeal(meal);

        dailyRecordRepository.save(dailyRecord);

        return meal;
    }


    private void createMeals(DailyRecord dailyRecord) {

        for (MealType type : MealType.values()) {

            MealRecord meal = MealRecord.of(
                    type,
                    defaultTime(type),
                    dailyRecord
            );

            meal.addFoodPortion(
                    chicken(),
                    100.0,
                    MeasureUnit.GRAM
            );

            dailyRecord.addMeal(meal);
        }
    }


    private LocalDateTime defaultTime(MealType type) {

        return switch (type) {

            case BREAKFAST ->
                    LocalDateTime.now()
                            .withHour(8)
                            .withMinute(0);

            case MID_MORNING ->
                    LocalDateTime.now()
                            .withHour(10)
                            .withMinute(30);

            case LUNCH ->
                    LocalDateTime.now()
                            .withHour(13)
                            .withMinute(0);

            case SNACK ->
                    LocalDateTime.now()
                            .withHour(17)
                            .withMinute(0);

            case DINNER ->
                    LocalDateTime.now()
                            .withHour(20)
                            .withMinute(0);
        };
    }


    private Food chicken() {

        return foodRepository.findByName("Chicken Breast")
                .orElseGet(() ->
                        foodRepository.save(
                                Food.of(
                                        "Chicken Breast",
                                        165,
                                        31,
                                        0,
                                        3,
                                        com.lirium.nutrition.model.enums.FoodCategory.PROTEIN,
                                        EnumSet.allOf(MealType.class)
                                )
                        )
                );
    }
}
