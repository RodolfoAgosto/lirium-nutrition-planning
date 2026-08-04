-- =====================================================================
-- V1__init_schema.sql
-- Esquema inicial de Lirium Nutrition
-- Generado a partir del DDL de Hibernate (ddl-auto=create) y revisado
-- para producción: nombres de constraints legibles en vez de hashes
-- autogenerados (FKxxxxxxxxxx), formato consistente.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Secuencias
-- ---------------------------------------------------------------------
create sequence daily_plan_seq start with 1 increment by 1;
create sequence daily_record_seq start with 1 increment by 1;
create sequence food_portion_seq start with 1 increment by 1;
create sequence food_seq start with 1 increment by 1;
create sequence meal_seq start with 1 increment by 1;
create sequence nutrition_plan_seq start with 1 increment by 1;
create sequence nutrition_plan_template_seq start with 1 increment by 1;
create sequence patient_profile_histories_seq start with 1 increment by 50;
create sequence plan_food_portion_seq start with 1 increment by 1;
create sequence plan_meal_seq start with 1 increment by 1;
create sequence restriction_seq start with 1 increment by 1;

-- ---------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------
create table daily_plans (
                             id bigint not null,
                             nutrition_plan_id bigint not null,
                             day_of_week varchar(255) not null check (day_of_week in ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
                             primary key (id)
);

create table daily_records (
                               record_date date not null,
                               created_at timestamp(6) not null,
                               id bigint not null,
                               patient_profile_id bigint not null,
                               updated_at timestamp(6) not null,
                               created_by varchar(255),
                               updated_by varchar(255),
                               primary key (id),
                               constraint uk_daily_record_patient_date unique (patient_profile_id, record_date)
);

create table food_portion_records (
                                      quantity float(53) not null,
                                      food_id bigint not null,
                                      id bigint not null,
                                      meal_record_id bigint not null,
                                      measure_unit varchar(255) check (measure_unit in ('GRAM','MILLILITER','UNIT')),
                                      primary key (id)
);

create table food_suitable_for (
                                   food_id bigint not null,
                                   meal_type varchar(255) check (meal_type in ('BREAKFAST','MID_MORNING','LUNCH','SNACK','DINNER'))
);

create table food_tags (
                           food_id bigint not null,
                           tag varchar(255) check (tag in ('GLUTEN','LACTOSE','MEAT','FISH','EGG','HONEY','GELATIN','NUTS','SOY','ALCOHOL')),
                           constraint uk_food_tags_food_tag unique (food_id, tag)
);

create table foods (
                       calories_per_100g integer not null,
                       carbs_per_100g integer not null,
                       default_unit smallint check (default_unit between 0 and 2),
                       density float(53),
                       fat_per_100g integer not null,
                       protein_per_100g integer not null,
                       unit_weight float(53),
                       created_at timestamp(6) not null,
                       id bigint not null,
                       updated_at timestamp(6) not null,
                       name varchar(120) not null unique,
                       category varchar(255) not null check (category in ('PROTEIN','CARB','DAIRY','VEGETABLE','FRUIT','SWEET','FAT','BEVERAGE')),
                       primary key (id)
);

create table meal_records (
                              overridden boolean not null,
                              created_at timestamp(6) not null,
                              daily_record_id bigint not null,
                              eaten_at timestamp(6) not null,
                              id bigint not null,
                              updated_at timestamp(6) not null,
                              notes varchar(500),
                              meal_type varchar(255) not null check (meal_type in ('BREAKFAST','MID_MORNING','LUNCH','SNACK','DINNER')),
                              primary key (id)
);

create table nutrition_plan_template (
                                         carb_percentage integer not null,
                                         fat_percentage integer not null,
                                         protein_percentage integer not null,
                                         created_at timestamp(6) not null,
                                         id bigint not null,
                                         updated_at timestamp(6) not null,
                                         created_by varchar(255),
                                         description varchar(255) not null,
                                         name varchar(255) not null unique,
                                         target_goal varchar(255) check (target_goal in ('WEIGHT_LOSS','MUSCLE_GAIN','WEIGHT_MAINTENANCE','METABOLIC_HEALTH','PREGNANCY_HEALTH','LACTATION_HEALTH')),
                                         updated_by varchar(255),
                                         primary key (id)
);

create table nutrition_plan_template_excluded_tags (
                                                       nutrition_plan_template_id bigint not null,
                                                       food_tag varchar(255) check (food_tag in ('GLUTEN','LACTOSE','MEAT','FISH','EGG','HONEY','GELATIN','NUTS','SOY','ALCOHOL'))
);

create table nutrition_plans (
                                 carb_grams integer not null,
                                 daily_calories integer not null,
                                 end_date date,
                                 fat_grams integer not null,
                                 protein_grams integer not null,
                                 start_date date,
                                 created_at timestamp(6) not null,
                                 id bigint not null,
                                 patient_profile_id bigint not null,
                                 updated_at timestamp(6) not null,
                                 created_by varchar(255),
                                 description varchar(255),
                                 name varchar(255),
                                 status varchar(255) not null check (status in ('DRAFT','ACTIVE','INACTIVE')),
                                 target_goal varchar(255) check (target_goal in ('WEIGHT_LOSS','MUSCLE_GAIN','WEIGHT_MAINTENANCE','METABOLIC_HEALTH','PREGNANCY_HEALTH','LACTATION_HEALTH')),
                                 updated_by varchar(255),
                                 primary key (id)
);

create table patient_profile (
                                 grams integer,
                                 height integer,
                                 created_at timestamp(6) not null,
                                 updated_at timestamp(6) not null,
                                 user_id bigint not null,
                                 activity_level varchar(255) check (activity_level in ('SEDENTARY','MODERATE','ACTIVE','VERY_ACTIVE')),
                                 created_by varchar(255),
                                 medical_notes TEXT,
                                 primary_goal varchar(255) check (primary_goal in ('WEIGHT_LOSS','MUSCLE_GAIN','WEIGHT_MAINTENANCE','METABOLIC_HEALTH','PREGNANCY_HEALTH','LACTATION_HEALTH')),
                                 sex varchar(255) check (sex in ('MALE','FEMALE')),
                                 updated_by varchar(255),
                                 primary key (user_id)
);

create table patient_profile_histories (
                                           grams integer,
                                           height integer,
                                           visit_date date not null,
                                           id bigint not null,
                                           patient_profile_id bigint not null,
                                           medical_notes TEXT,
                                           primary_goal varchar(255) check (primary_goal in ('WEIGHT_LOSS','MUSCLE_GAIN','WEIGHT_MAINTENANCE','METABOLIC_HEALTH','PREGNANCY_HEALTH','LACTATION_HEALTH')),
                                           primary key (id)
);

create table patient_profile_history_conditions (
                                                    patient_profile_history_id bigint not null,
                                                    physiological_condition varchar(255) check (physiological_condition in ('PREGNANCY','LACTATION','MENOPAUSE'))
);

create table patient_profile_history_restrictions (
                                                      history_id bigint not null,
                                                      restriction_id bigint not null,
                                                      primary key (history_id, restriction_id)
);

create table patient_profile_physiological_conditions (
                                                          patient_profile_id bigint not null,
                                                          physiological_condition varchar(255) check (physiological_condition in ('PREGNANCY','LACTATION','MENOPAUSE'))
);

create table patient_profile_restriction (
                                             patient_profile_id bigint not null,
                                             restriction_id bigint not null,
                                             primary key (patient_profile_id, restriction_id)
);

create table plan_food_portions (
                                    quantity float(53) not null,
                                    food_id bigint not null,
                                    id bigint not null,
                                    plan_meal_id bigint not null,
                                    measure_unit varchar(255) check (measure_unit in ('GRAM','MILLILITER','UNIT')),
                                    primary key (id)
);

create table plan_meals (
                            created_at timestamp(6) not null,
                            daily_plan_id bigint not null,
                            id bigint not null,
                            updated_at timestamp(6) not null,
                            meal_type varchar(255) not null check (meal_type in ('BREAKFAST','MID_MORNING','LUNCH','SNACK','DINNER')),
                            primary key (id)
);

create table refresh_tokens (
                                revoked boolean not null,
                                expires_at timestamp(6) with time zone not null,
                                id bigint generated by default as identity,
                                user_id bigint not null unique,
                                token varchar(255) not null unique,
                                primary key (id)
);

create table restriction_excluded_tags (
                                           restriction_id bigint not null,
                                           food_tag varchar(255) check (food_tag in ('GLUTEN','LACTOSE','MEAT','FISH','EGG','HONEY','GELATIN','NUTS','SOY','ALCOHOL'))
);

create table restrictions (
                              created_at timestamp(6) not null,
                              id bigint not null,
                              updated_at timestamp(6) not null,
                              code varchar(50) not null unique,
                              name varchar(80) not null,
                              category varchar(255) not null check (category in ('PATHOLOGICAL','INTOLERANCES','DIETARY')),
                              description varchar(255) not null,
                              primary key (id)
);

create table users (
                       birth_date date,
                       email_validated boolean not null,
                       enabled boolean not null,
                       created_at timestamp(6) not null,
                       id bigint generated by default as identity,
                       updated_at timestamp(6) not null,
                       dni varchar(20) unique,
                       first_name varchar(60),
                       last_name varchar(60),
                       email varchar(100) not null unique,
                       password_hash varchar(200),
                       created_by varchar(255),
                       role varchar(255) check (role in ('PATIENT','NUTRITIONIST','ADMIN')),
                       updated_by varchar(255),
                       primary key (id)
);

-- ---------------------------------------------------------------------
-- Foreign keys (nombradas explícitamente en vez de los hashes FKxxxx
-- que genera Hibernate automáticamente)
-- ---------------------------------------------------------------------
alter table if exists daily_plans
    add constraint fk_daily_plan_nutrition_plan
    foreign key (nutrition_plan_id) references nutrition_plans;

alter table if exists daily_records
    add constraint fk_daily_record_patient_profile
    foreign key (patient_profile_id) references patient_profile;

alter table if exists food_portion_records
    add constraint fk_food_portion_record_food
    foreign key (food_id) references foods;

alter table if exists food_portion_records
    add constraint fk_food_portion_record_meal_record
    foreign key (meal_record_id) references meal_records;

alter table if exists food_suitable_for
    add constraint fk_food_suitable_for_food
    foreign key (food_id) references foods;

alter table if exists food_tags
    add constraint fk_food_tags_food
    foreign key (food_id) references foods;

alter table if exists meal_records
    add constraint fk_meal_record_daily_record
    foreign key (daily_record_id) references daily_records;

alter table if exists nutrition_plan_template_excluded_tags
    add constraint fk_nutrition_plan_template_excluded_tags_template
    foreign key (nutrition_plan_template_id) references nutrition_plan_template;

alter table if exists nutrition_plans
    add constraint fk_nutrition_plan_patient_profile
    foreign key (patient_profile_id) references patient_profile;

alter table if exists patient_profile
    add constraint fk_patient_profile_user
    foreign key (user_id) references users;

alter table if exists patient_profile_histories
    add constraint fk_patient_profile_history_patient_profile
    foreign key (patient_profile_id) references patient_profile;

alter table if exists patient_profile_history_conditions
    add constraint fk_patient_profile_history_conditions_history
    foreign key (patient_profile_history_id) references patient_profile_histories;

alter table if exists patient_profile_history_restrictions
    add constraint fk_patient_profile_history_restriction_restriction
    foreign key (restriction_id) references restrictions;

alter table if exists patient_profile_history_restrictions
    add constraint fk_patient_profile_history_restriction_history
    foreign key (history_id) references patient_profile_histories;

alter table if exists patient_profile_physiological_conditions
    add constraint fk_patient_profile_physiological_conditions_profile
    foreign key (patient_profile_id) references patient_profile;

alter table if exists patient_profile_restriction
    add constraint fk_patient_profile_restriction_restriction
    foreign key (restriction_id) references restrictions;

alter table if exists patient_profile_restriction
    add constraint fk_patient_profile_restriction_patient_profile
    foreign key (patient_profile_id) references patient_profile;

alter table if exists plan_food_portions
    add constraint fk_plan_food_portion_food
    foreign key (food_id) references foods;

alter table if exists plan_food_portions
    add constraint fk_plan_food_portion_plan_meal
    foreign key (plan_meal_id) references plan_meals;

alter table if exists plan_meals
    add constraint fk_plan_meal_daily_plan
    foreign key (daily_plan_id) references daily_plans;

alter table if exists refresh_tokens
    add constraint fk_refresh_token_user
    foreign key (user_id) references users;

alter table if exists restriction_excluded_tags
    add constraint fk_restriction_excluded_tags_restriction
    foreign key (restriction_id) references restrictions;