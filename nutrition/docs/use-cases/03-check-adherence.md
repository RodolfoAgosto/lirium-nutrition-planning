# Check Adherence

Two read-only reports that compare what the patient recorded against what was planned, for a date range.
Available to the **patient** for their own data, and to nutritionists and admins for any patient.

## Adherence report

Measures how consistently the patient records their meals.

- Expected meals: 5 per day (one per meal type) across the whole range.
- Recorded meals: meals present in the daily records, excluding those marked as overridden.
- Adherence: recorded / expected, as a percentage, with a day-by-day breakdown.

## Nutrition comparison report

Compares, day by day, the nutrients actually consumed against the active plan's targets
(calories, protein, carbs and fat).

## Business rules

- `from` must be on or before `to`.
- The adherence range can't start before the patient's first plan start date.
- The nutrition comparison requires an `ACTIVE` plan and only covers dates from that plan's start date onwards.
- Reports are calculated on demand and never modify plans or records.

## Endpoints

- `GET /api/patients/{patientId}/daily-records/adherence?from=&to=` — adherence report
- `GET /api/patients/{patientId}/daily-records/nutrition-comparison?from=&to=` — nutrition comparison
