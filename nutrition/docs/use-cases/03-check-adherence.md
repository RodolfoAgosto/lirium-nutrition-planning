# Check Adherence

Two read-only reports that compare what the patient recorded against what was planned, for a date range.
Available to the **patient** for their own data, and to nutritionists and admins for any patient.

## Adherence report

Measures how many of the planned meals the patient followed, day by day.

- **Expected meals** come from the plan in effect on each date: the meals its daily plan prescribes
  for that day of the week, or 0 when no plan applied.
- **Followed meals** are meals eaten as planned, without changes (`MealRecord.followsPlan()`).
  Modified meals are counted separately.
- **No data is not zero.** Days without a record have `hasRecord = false` and `followedMeals` /
  `modifiedMeals` set to `null`.
- **Summary.** `adherence` is followed / expected over the whole range, so it mixes logging
  consistency and compliance. `adherenceOnRecordedDays` uses recorded days only: how well the
  patient follows the plan when they do log. Compare it with `recordedDays` / `totalDays`.

## Nutrition comparison report

Compares, day by day, what the patient recorded against what the active plan prescribes for that
day of the week (calories, protein, carbs and fat).

- **Planned vs. targets.** Each day shows what the plan prescribes for it (`planned`). The plan's
  daily targets are returned once (`targets`) as reference: the generated plan approximates them,
  so planned and target can differ slightly.
- **Score against the plan, not the targets.** Each nutrient gets a 0-100 closeness score: 100
  when consumption matches what was planned, decreasing equally for under- and over-eating. The
  overall score weights calories at 40% and each macro at 20% (illustrative weights, not clinical
  guidance). Scoring against the plan measures the patient's compliance only; the distance between
  plan and targets belongs to the generator, not to the patient.
- **No data is not zero.** A day the patient never opened has `hasRecord = false`, and `consumed`
  and `score` are `null`. `score` is also `null` when nothing was planned for that day.
- **Summary.** `averageConsumed` averages recorded days only, and `averageScore` averages scored
  days only. Averages are `null` when there is no data. `recordedDays` / `totalDays` shows logging
  consistency.

## Business rules

- `from` and `to` are optional: `to` defaults to today and `from` to 29 days before `to`, so a
  request without dates returns the last 30 days.
- `from` must be on or before `to`.
- Both reports trim a range that starts before the relevant plan and return the effective `from`:
  the patient's first plan for adherence, the `ACTIVE` plan for the nutrition comparison. A range
  that ends before it returns an empty report.
- The nutrition comparison requires an `ACTIVE` plan.
- Reports are calculated on demand and never modify plans or records.

## Endpoints

- `GET /api/patients/{patientId}/daily-records/meal-adherence?from=&to=` — meal adherence report
- `GET /api/patients/{patientId}/daily-records/nutrition-comparison?from=&to=` — nutrition comparison