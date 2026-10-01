# Check Adherence

Two read-only reports that compare what the patient recorded against what was planned, for a date range.
Available to the **patient** for their own data, and to nutritionists and admins for any patient.

## Adherence report

Measures how consistently the patient records their meals.

- Expected meals: 5 per day (one per meal type) across the whole range.
- Recorded meals: meals present in the daily records, excluding those marked as overridden.
- Adherence: recorded / expected, as a percentage, with a day-by-day breakdown.

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

- `from` must be on or before `to`.
- The adherence range can't start before the patient's first plan start date.
- The nutrition comparison requires an `ACTIVE` plan. If the range starts before the plan, it is
  trimmed to the plan's start date and the effective `from` is returned.
- Reports are calculated on demand and never modify plans or records.

## Endpoints

- `GET /api/patients/{patientId}/daily-records/adherence?from=&to=` — adherence report
- `GET /api/patients/{patientId}/daily-records/nutrition-comparison?from=&to=` — nutrition comparison
