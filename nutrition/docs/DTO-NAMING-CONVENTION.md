# DTO Naming Convention

## Request DTOs

`{Entity}{Action}RequestDTO`

Input data for a specific operation.

Examples:

* `FoodCreateRequestDTO`
* `FoodUpdateRequestDTO`
* `NutritionPlanCompleteRequestDTO`

## Response DTOs

`{Entity}ResponseDTO`

General resource response, including its ID and summarized aggregated entities.

Examples:

* `FoodResponseDTO`
* `DailyPlanResponseDTO`
* `NutritionPlanResponseDTO`

## Summary DTOs

`{Entity}SummaryDTO`

Abbreviated representation intended for list endpoints.

Examples:

* `FoodSummaryDTO`
* `DailyPlanSummaryDTO`
* `PatientSummaryDTO`

## Detail DTOs

`{Entity}DetailDTO`

Detailed representation including complete related entities.

Examples:

* `DailyPlanDetailDTO`
* `NutritionPlanDetailDTO`
* `PlanMealDetailDTO`

## Report DTOs

`{Concept}ReportDTO`

Result of a specific report or analysis, generally generated from existing data and not necessarily associated with a direct representation of an entity.

Examples:

* `AdherenceReportDTO`
* `NutritionComparisonReportDTO`

## Concept DTOs

`{Concept}DTO`

Simple representation of a concept, catalog item, or enum.

Examples:

* `PhysiologicalConditionDTO`
* `RestrictionDTO`
