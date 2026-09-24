# Record Daily Intake

The patient records what they actually ate on a given day. Performed by the **patient** on their own records
(nutritionists and admins can do it for any patient).

## Flow

1. The patient opens a date. If there's no `DailyRecord` for that date yet, the system creates it
   and pre-fills it with the meals planned for that weekday in the active plan.
2. The patient adjusts each meal to what was actually eaten: adds or removes food portions, adds notes,
   or marks a meal as overridden (not eaten as planned).
3. The system persists the changes through the `DailyRecord` aggregate.

Structure: `DailyRecord → MealRecord → FoodPortionRecord`

## Business rules

- There is one `DailyRecord` per patient per date: opening an existing date returns the same record.
- Records can't be created for future dates, and meals can't be recorded in the future.
- A record can only be created while the patient has an `ACTIVE` plan,
  and never for a date before that plan's start date.
- Portion quantities must be positive.
- Recorded intake is independent from the plan: editing the plan later doesn't change what was recorded.

## Endpoints

- `POST /api/patients/{patientId}/daily-records/ensure` — get or create the record for a date
- `GET /api/patients/{patientId}/daily-records` — patient's records
- `GET /api/daily-records/{id}` — record detail
- `PATCH /api/daily-records/meals/{mealRecordId}` — update meal (notes, override)
- `POST /api/daily-records/meals/{mealRecordId}/portions` — add food portion
- `DELETE /api/daily-records/{dailyRecordId}/meals/{mealRecordId}/portions/{portionId}` — remove food portion
