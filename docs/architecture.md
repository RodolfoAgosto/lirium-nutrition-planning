# Architecture

[← Back to README](../README.md)

## Package dependencies

Each package depends only on the ones below it. The rules are enforced by [`ArchitectureTest`](../nutrition/src/test/java/com/lirium/nutrition/ArchitectureTest.java) (ArchUnit) on every build.

```mermaid
flowchart TD
    C[controller<br/>REST endpoints · OpenAPI] --> S[service<br/>use cases · generation engine]
    C --> DTO[dto<br/>request / response records]
    S --> R[repository<br/>Spring Data JPA]
    S --> MAP[mapper<br/>MapStruct]
    S --> M[model<br/>entities · value objects · enums]
    MAP --> DTO
    MAP --> M
    DTO --> M
    R --> M
    I[infrastructure<br/>security · config] --> R
    I --> S
    M --> EX[exception<br/>domain exceptions · ApiError]
    classDef web fill:#E3F2FD,stroke:#1E88E5,color:#0D47A1
    classDef app fill:#E8F5E9,stroke:#2E7D32,color:#1B2F21
    classDef domain fill:#2E7D32,stroke:#1B2F21,color:#fff
    classDef infra fill:#FFF3E0,stroke:#EF6C00,color:#E65100
    class C,DTO web
    class S,MAP,R app
    class M,EX domain
    class I infra
```

| Rule (ArchUnit) | What it guarantees |
|---|---|
| Layered architecture | No layer depends on `controller`. `service` is reached only from `controller` and `infrastructure`. `repository` is reached only from `service` and `infrastructure`. |
| Security beans don't depend on services | Ownership checks (`*Security`) query repositories directly, so `@PreAuthorize` never triggers business logic. |
| Controllers | `@RestController` classes live in `..controller..` and end with `Controller`. |
| Services | `*ServiceImpl` classes live in `..service.impl..` and implement their `*Service` interface. |
| Repositories | Everything in `..repository..` is an interface. |

`exception` is used by most layers (domain exceptions are thrown from the model and translated to HTTP by the global handler). Its arrows are omitted from the diagram, except the one from `model`, for readability.

## Domain model

Aggregate roots are marked `<<aggregate root>>`. Value objects are Java `record`s embedded with `@Embedded`.

```mermaid
classDiagram
    direction TB

    class User {
        Long id
        String email
        String passwordHash
        String firstName
        String lastName
        LocalDate birthDate
        String dni
        Role role
        boolean enabled
    }
    class PatientProfile {
        <<aggregate root>>
        Long id = user.id
        Sex sex
        ActivityLevel activityLevel
        Weight weight
        Height height
        String medicalNotes
        Set~PhysiologicalCondition~ physiologicalConditions
        GoalType primaryGoal
        addRestriction(Restriction)
        updateNutritionProfile(...)
    }
    class PatientProfileHistory {
        LocalDate visitDate
        Weight weight
        Height height
        GoalType primaryGoal
    }
    class Restriction {
        String code
        String name
        RestrictionCategory category
        Set~FoodTag~ excludedTags
    }
    class Food {
        String name
        boolean active
        int caloriesPer100g
        int proteinPer100g
        int carbsPer100g
        int fatPer100g
        FoodCategory category
        Double minServingGrams
        Double maxServingGrams
        MeasureUnit defaultUnit
        Set~MealType~ suitableFor
        Set~FoodTag~ foodTags
        toGrams(quantity, unit) Double
        deactivate()
    }
    class NutritionPlanTemplate {
        String name
        GoalType targetGoal
        int proteinPercentage
        int carbPercentage
        int fatPercentage
        Set~FoodTag~ excludedTags
        boolean active
    }
    class NutritionPlan {
        <<aggregate root>>
        String name
        PlanStatus status
        LocalDate startDate
        LocalDate endDate
        GoalType targetGoal
        int dailyCalories
        int proteinGrams
        int carbGrams
        int fatGrams
        activate(startDate)
        close(endDate)
        complete(name, description, endDate)
        ensureEditable()
    }
    class DailyPlan {
        DayOfWeek dayOfWeek
        addMeal(PlanMeal) PlanMeal
        removeMeal(PlanMeal)
    }
    class PlanMeal {
        <<aggregate root>>
        MealType type
        addFoodPortion(PlanFoodPortion)
        removeFoodPortion(PlanFoodPortion)
    }
    class AbstractFoodPortion {
        <<abstract>>
        Double quantity
        MeasureUnit measureUnit
        grams() Double
        calories() Calories
        protein() Protein
        carbs() Carbs
        fat() Fat
    }
    class PlanFoodPortion {
        changeQuantity(quantity)
        changeFood(Food)
    }
    class DailyRecord {
        <<aggregate root>>
        LocalDate date
        addMeal(MealRecord)
    }
    class MealRecord {
        MealType type
        boolean overridden
        String notes
        LocalDateTime eatenAt
        addFoodPortion(food, quantity, unit)
        markAsOverridden(reason)
    }
    class FoodPortionRecord
    class RefreshToken {
        String token
        Instant expiresAt
        boolean revoked
        isExpired() boolean
        revoke()
    }

    User "1" -- "0..1" PatientProfile : shares id (@MapsId)
    User "1" --> "*" RefreshToken
    PatientProfile "*" --> "*" Restriction
    PatientProfile "1" --> "*" PatientProfileHistory
    PatientProfile "1" --> "*" NutritionPlan
    PatientProfile "1" --> "*" DailyRecord
    NutritionPlan "1" *-- "7" DailyPlan
    DailyPlan "1" *-- "0..5" PlanMeal : one per MealType
    PlanMeal "1" *-- "*" PlanFoodPortion : no duplicate food
    DailyRecord "1" *-- "*" MealRecord
    MealRecord "1" *-- "*" FoodPortionRecord
    AbstractFoodPortion <|-- PlanFoodPortion
    AbstractFoodPortion <|-- FoodPortionRecord
    AbstractFoodPortion --> Food
```

### Value objects

```mermaid
classDiagram
    direction LR
    class Weight {
        <<record>>
        int grams
    }
    class Height {
        <<record>>
        int cm
    }
    class Calories {
        <<record>>
        int amount
    }
    class Protein {
        <<record>>
        int grams
    }
    class Carbs {
        <<record>>
        int amount
    }
    class Fat {
        <<record>>
        int amount
    }
    class MacroDistribution {
        <<record>>
        int proteinGrams
        int carbGrams
        int fatGrams
    }
    class NutrientBudget {
        <<record>>
        Calories calories
        Carbs carbs
        Fat fat
        Protein protein
    }
    class MacroDeviation {
        <<record>>
        double calories
        double carbs
        double fat
        double protein
    }
    class MealAssemblyResult {
        <<record>>
        NutrientBudget consumed
        MacroDeviation deviation
    }
    NutrientBudget --> Calories
    NutrientBudget --> Protein
    NutrientBudget --> Carbs
    NutrientBudget --> Fat
    MealAssemblyResult --> NutrientBudget
    MealAssemblyResult --> MacroDeviation
```

`NutrientBudget`, `MacroDeviation` and `MealAssemblyResult` carry the engine's state from meal to meal: each meal consumes part of the daily budget, and its deviation carries over to the next meal.

### Enums

| Enum | Values |
|---|---|
| `PlanStatus` | `DRAFT`, `ACTIVE`, `INACTIVE` |
| `GoalType` | `WEIGHT_LOSS`, `MUSCLE_GAIN`, `WEIGHT_MAINTENANCE`, `METABOLIC_HEALTH`, `PREGNANCY_HEALTH`, `LACTATION_HEALTH` |
| `ActivityLevel` | `SEDENTARY`, `MODERATE`, `ACTIVE`, `VERY_ACTIVE` |
| `PhysiologicalCondition` | `PREGNANCY`, `LACTATION`, `MENOPAUSE` |
| `MealType` | `BREAKFAST`, `MID_MORNING`, `LUNCH`, `SNACK`, `DINNER` |
| `FoodCategory` | `PROTEIN`, `CARB`, `DAIRY`, `VEGETABLE`, `FRUIT`, `SWEET`, `FAT`, `BEVERAGE` |
| `FoodTag` | `GLUTEN`, `LACTOSE`, `MEAT`, `FISH`, `EGG`, `HONEY`, `GELATIN`, `NUTS`, `SOY`, `ALCOHOL`, `LEGUME`, `HIGH_PROTEIN` |
| `RestrictionCategory` | `PATHOLOGICAL`, `INTOLERANCES`, `DIETARY` |
| `Role` | `PATIENT`, `NUTRITIONIST`, `ADMIN` |

A `Restriction` excludes a set of `FoodTag`s. The generator never selects a `Food` carrying any of them.