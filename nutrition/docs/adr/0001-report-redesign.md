# ADR 0001: Redesign of the adherence and nutrition comparison reports

**Status:** Accepted

## Context

Both reports produced numbers that could mislead a nutritionist:

- A day the patient never logged showed zero intake, indistinguishable from not eating.
- The nutrition score compared intake with the plan's theoretical targets. A patient who ate
  exactly what the plan prescribed still lost points, because the generator only approximates
  the targets.
- The score only looked at calories: twice the planned protein could still score close to 100.
- The adherence percentage mixed two problems: not logging and not following the plan.
- `recordedMeals` actually counted meals followed as planned, the rule lived in a service
  filter, and five meals were expected every day regardless of the plan.
- The two reports handled a range starting before the plan differently (trim vs. 400).

## Decision

1. **Score against the planned intake, not the targets.** Each day is compared with what its
   daily plan prescribes. The distance between plan and targets belongs to the generator;
   targets are returned once, as reference.
2. **Score each nutrient.** A 0-100 closeness score for calories, protein, carbs and fat, plus a
   weighted overall (calories 40%, each macro 20%; illustrative weights). The formula lives in
   one domain method, `NutritionScore.closeness()`.
3. **Null for missing data, never zero.** `hasRecord` plus `null` consumption and scores.
   Absence is modeled at object level in the nutrition report, at field level in adherence.
4. **Averages in the backend, over days with data.** Both reports return a summary that
   separates logging consistency (`recordedDays`) from compliance.
5. **Business rules in the domain.** `DailyPlan.plannedNutrients()`,
   `NutritionPlan.isInEffectOn()`, `NutritionPlan.plannedMealCountFor()`,
   `MealRecord.followsPlan()`.
6. **One range rule for both reports:** trim to the plan and return the effective `from`.

## Alternatives considered

- **Keep scoring against targets:** simpler, but blames the patient for the generator.
- **Let the front end compute averages:** every client would need the same rules; they would
  drift.
- **Asymmetric score by goal and a tolerance band:** both need clinical parameters we can't
  validate. Left out on purpose; adding them is a local change in `closeness()`.

## Consequences

- Breaking change in both response contracts, made before any front end consumed them.
- Each number now answers one question, and the summary can't be skewed by unlogged days.
- The generator's precision is visible separately (`planned` vs. `targets`) instead of hidden in
  the patient's score.