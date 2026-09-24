# Manage Nutrition Plan

Covers the lifecycle of the `NutritionPlan` aggregate: generation, meal editing, activation and completion.
Performed by a **nutritionist** (or admin). Patients can only read their own plans.

```
DRAFT ──activate──▶ ACTIVE ──complete / replaced──▶ INACTIVE
```

## 1. Generate plan

Creates a new plan for a patient, either from scratch or from a template.

1. The nutritionist requests a plan for a patient (optionally based on a template).
2. The system validates the patient profile has weight, height, activity level and primary goal.
3. The system calculates the daily energy requirement from the patient profile: sex, weight, height,
   age, activity level, primary goal and physiological conditions.
4. The system distributes those calories into protein, carbs and fat
   (using the template's distribution when a template is used).
5. The system assembles the weekly structure `NutritionPlan → DailyPlan → PlanMeal → PlanFoodPortion`,
   selecting foods that respect the patient's dietary restrictions.
6. The plan is persisted as a single aggregate in `DRAFT` status.

**Business rules**

*Energy requirement*
- Basal metabolic rate is calculated with the Mifflin-St Jeor equation (sex, weight, height and age)
  and multiplied by the activity level factor.
- The result is adjusted by the primary goal (e.g. −20% for weight loss, +15% for muscle gain)
  and by each physiological condition (e.g. pregnancy, lactation).

*Macronutrients*
- From scratch: protein is set in grams per kg of body weight and fat as a percentage of calories,
  both according to sex and activity level; carbs take the remaining calories.
- From a template: the template's protein / carbs / fat percentages are used.

*Food selection*
- Foods carrying a tag excluded by any of the patient's dietary restrictions are never selected.
- When using a template, the template's excluded tags are also applied.
- Each meal only uses foods suitable for that meal type (breakfast, lunch, etc.).
- Each meal gets its share of the daily budget; any deviation carries over to the next meal.
- Variety: a food isn't repeated within the same day and has a weekly frequency limit.
- Portions stay within each food's minimum and maximum serving size.

*Lifecycle*
- A plan is always created as `DRAFT`, with a default name: the template name or the goal
  (e.g. "Weight loss plan").
- A patient can't have more than one `DRAFT` plan. An `ACTIVE` plan doesn't block generation:
  the new draft replaces it when activated.
- A plan can't be generated with an incomplete patient profile.
- Plans created from a template are independent: later template changes don't affect them.

## 2. Edit plan meals

The nutritionist adjusts the generated plan: adds or removes meals, adds or removes food portions, and changes quantities.

**Business rules**
- Only `DRAFT` plans can be edited.
- `PlanMeal` is the aggregate root for food portion writes; portions are never modified directly.
- A day can't have two meals of the same type (e.g. two lunches).
- The same food can't appear twice in the same meal.
- A new meal is created empty; foods are added to it afterwards.

## 3. Activate / complete plan

**Business rules**
- Only `DRAFT` plans can be activated. An `INACTIVE` plan can't be reactivated:
  to resume it, a new plan is generated.
- A patient has at most one `ACTIVE` plan: activating a new one closes the previous one
  (it becomes `INACTIVE`, with its end date set to the day before).
- Only `ACTIVE` plans can be completed. Completing sets the end date and moves the plan to `INACTIVE`.

## Endpoints

- `POST /api/nutrition-plans/generate/{patientId}` — generate from scratch
- `POST /api/nutrition-plans/generate-from-template/{patientId}/{templateId}` — generate from template
- `POST /api/plan-meals` — add meal
- `DELETE /api/plan-meals/{id}` — remove meal
- `POST /api/plan-meals/{mealId}/portions` — add food portion
- `DELETE /api/plan-meals/{mealId}/portions/{portionId}` — remove food portion
- `PATCH /api/plan-meals/{mealId}/portions/{portionId}/quantity` — update quantity
- `PATCH /api/nutrition-plans/{id}/activate` — activate
- `PATCH /api/nutrition-plans/{id}/complete` — complete
- `GET /api/nutrition-plans/{id}` — plan detail
- `GET /api/patients/{patientId}/nutrition-plans` — patient's plans
- `GET /api/patients/{patientId}/nutrition-plans/active` — patient's active plan