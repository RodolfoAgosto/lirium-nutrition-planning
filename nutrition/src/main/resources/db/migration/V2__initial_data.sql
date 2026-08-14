-- ---------------------------------------------------------------------
-- Foods (37 filas)
-- ---------------------------------------------------------------------

INSERT INTO foods (
    calories_per_100g,
    carbs_per_100g,
    default_unit,
    density,
    fat_per_100g,
    protein_per_100g,
    unit_weight,
    created_at,
    id,
    updated_at,
    name,
    category,
    min_serving_grams,
    max_serving_grams
) VALUES
      (165, 0, 0, NULL, 4, 31, NULL, TIMESTAMP '2026-08-02 16:51:58.084869', 1, TIMESTAMP '2026-08-02 16:51:58.084869', 'Chicken Breast', 'PROTEIN', 80, 250),
      (208, 0, 0, NULL, 13, 20, NULL, TIMESTAMP '2026-08-02 16:51:58.08687', 2, TIMESTAMP '2026-08-02 16:51:58.08687', 'Salmon', 'PROTEIN', 80, 250),
      (116, 0, 0, NULL, 1, 26, NULL, TIMESTAMP '2026-08-02 16:51:58.08687', 3, TIMESTAMP '2026-08-02 16:51:58.08687', 'Tuna in Water', 'PROTEIN', 60, 180),
      (254, 0, 0, NULL, 20, 17, NULL, TIMESTAMP '2026-08-02 16:51:58.087869', 4, TIMESTAMP '2026-08-02 16:51:58.087869', 'Ground Beef', 'PROTEIN', 80, 250),
      (76, 2, 0, NULL, 5, 8, NULL, TIMESTAMP '2026-08-02 16:51:58.087869', 5, TIMESTAMP '2026-08-02 16:51:58.087869', 'Tofu', 'PROTEIN', 80, 300),
      (155, 1, 2, NULL, 11, 13, 60.0, TIMESTAMP '2026-08-02 16:51:58.087869', 6, TIMESTAMP '2026-08-02 16:51:58.087869', 'Egg', 'PROTEIN', 60, 120),
      (61, 5, 0, NULL, 3, 3, NULL, TIMESTAMP '2026-08-02 16:51:58.088869', 7, TIMESTAMP '2026-08-02 16:51:58.088869', 'Whole Milk Yogurt', 'DAIRY', 100, 350),
      (98, 3, 0, NULL, 4, 11, NULL, TIMESTAMP '2026-08-02 16:51:58.088869', 8, TIMESTAMP '2026-08-02 16:51:58.088869', 'Cottage Cheese', 'DAIRY', 60, 200),
      (100, 4, 0, NULL, 5, 10, NULL, TIMESTAMP '2026-08-02 16:51:58.088869', 9, TIMESTAMP '2026-08-02 16:51:58.088869', 'Greek Yogurt', 'DAIRY', 100, 300),
      (61, 5, 1, 1.03, 3, 3, NULL, TIMESTAMP '2026-08-02 16:51:58.089869', 10, TIMESTAMP '2026-08-02 16:51:58.089869', 'Whole Milk', 'DAIRY', 150, 400),
      (389, 66, 0, NULL, 7, 17, NULL, TIMESTAMP '2026-08-02 16:51:58.089869', 11, TIMESTAMP '2026-08-02 16:51:58.089869', 'Oatmeal', 'CARB', 30, 120),
      (216, 45, 0, NULL, 2, 5, NULL, TIMESTAMP '2026-08-02 16:51:58.089869', 12, TIMESTAMP '2026-08-02 16:51:58.089869', 'Brown Rice', 'CARB', 100, 300),
      (348, 67, 0, NULL, 3, 13, NULL, TIMESTAMP '2026-08-02 16:51:58.089869', 13, TIMESTAMP '2026-08-02 16:51:58.089869', 'Whole Wheat Pasta', 'CARB', 60, 200),
      (86, 20, 0, NULL, 0, 2, NULL, TIMESTAMP '2026-08-02 16:51:58.089869', 14, TIMESTAMP '2026-08-02 16:51:58.089869', 'Sweet Potato', 'CARB', 100, 400),
      (247, 41, 0, NULL, 4, 9, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 15, TIMESTAMP '2026-08-02 16:51:58.091379', 'Whole Grain Bread', 'CARB', 30, 180),
      (120, 22, 0, NULL, 0, 4, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 16, TIMESTAMP '2026-08-02 16:51:58.091379', 'Quinoa', 'CARB', 80, 250),
      (34, 7, 0, NULL, 0, 3, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 17, TIMESTAMP '2026-08-02 16:51:58.091379', 'Broccoli', 'VEGETABLE', 80, 250),
      (23, 4, 0, NULL, 0, 3, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 18, TIMESTAMP '2026-08-02 16:51:58.091379', 'Spinach', 'VEGETABLE', 50, 200),
      (18, 4, 0, NULL, 0, 1, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 19, TIMESTAMP '2026-08-02 16:51:58.091379', 'Tomato', 'VEGETABLE', 80, 250),
      (41, 10, 0, NULL, 0, 1, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 20, TIMESTAMP '2026-08-02 16:51:58.091379', 'Carrot', 'VEGETABLE', 50, 250),
      (17, 3, 0, NULL, 0, 1, NULL, TIMESTAMP '2026-08-02 16:51:58.091379', 21, TIMESTAMP '2026-08-02 16:51:58.091379', 'Zucchini', 'VEGETABLE', 80, 300),
      (31, 6, 0, NULL, 0, 1, NULL, TIMESTAMP '2026-08-02 16:51:58.092903', 22, TIMESTAMP '2026-08-02 16:51:58.092903', 'Bell Pepper', 'VEGETABLE', 50, 250),
      (52, 14, 2, NULL, 0, 0, 150.0, TIMESTAMP '2026-08-02 16:51:58.092903', 23, TIMESTAMP '2026-08-02 16:51:58.092903', 'Apple', 'FRUIT', 150, 300),
      (89, 23, 2, NULL, 0, 1, 120.0, TIMESTAMP '2026-08-02 16:51:58.092903', 24, TIMESTAMP '2026-08-02 16:51:58.092903', 'Banana', 'FRUIT', 120, 240),
      (47, 12, 2, NULL, 0, 1, 130.0, TIMESTAMP '2026-08-02 16:51:58.092903', 25, TIMESTAMP '2026-08-02 16:51:58.092903', 'Orange', 'FRUIT', 130, 260),
      (57, 15, 2, NULL, 0, 0, 140.0, TIMESTAMP '2026-08-02 16:51:58.092903', 26, TIMESTAMP '2026-08-02 16:51:58.092903', 'Pear', 'FRUIT', 140, 280),
      (160, 9, 0, NULL, 15, 2, NULL, TIMESTAMP '2026-08-02 16:51:58.093914', 27, TIMESTAMP '2026-08-02 16:51:58.093914', 'Avocado', 'FAT', 50, 150),
      (607, 21, 0, NULL, 54, 20, NULL, TIMESTAMP '2026-08-02 16:51:58.093914', 28, TIMESTAMP '2026-08-02 16:51:58.093914', 'Mixed Nuts', 'FAT', 15, 60),
      (588, 20, 0, NULL, 50, 25, NULL, TIMESTAMP '2026-08-02 16:51:58.093914', 29, TIMESTAMP '2026-08-02 16:51:58.093914', 'Peanut Butter', 'FAT', 15, 60),
      (884, 0, 1, 0.92, 100, 0, NULL, TIMESTAMP '2026-08-02 16:51:58.093914', 30, TIMESTAMP '2026-08-02 16:51:58.093914', 'Olive Oil', 'FAT', 5, 30),
      (471, 64, 0, NULL, 20, 10, NULL, TIMESTAMP '2026-08-02 16:51:58.093914', 31, TIMESTAMP '2026-08-02 16:51:58.093914', 'Honey Granola', 'SWEET', 20, 80),
      (546, 60, 0, NULL, 31, 5, NULL, TIMESTAMP '2026-08-02 16:51:58.094911', 32, TIMESTAMP '2026-08-02 16:51:58.094911', 'Dark Chocolate', 'SWEET', 10, 40),
      (130, 22, 0, NULL, 4, 3, NULL, TIMESTAMP '2026-08-02 16:51:58.094911', 33, TIMESTAMP '2026-08-02 16:51:58.094911', 'Rice Pudding', 'SWEET', 20, 100),
      (0, 0, 1, 1.0, 0, 0, NULL, TIMESTAMP '2026-08-02 16:51:58.094911', 34, TIMESTAMP '2026-08-02 16:51:58.094911', 'Water', 'BEVERAGE', 200, 1000),
      (1, 0, 1, 1.0, 0, 0, NULL, TIMESTAMP '2026-08-02 16:51:58.094911', 35, TIMESTAMP '2026-08-02 16:51:58.094911', 'Green Tea', 'BEVERAGE', 150, 500),
      (45, 10, 1, 1.04, 0, 1, NULL, TIMESTAMP '2026-08-02 16:51:58.09591', 36, TIMESTAMP '2026-08-02 16:51:58.09591', 'Orange Juice', 'BEVERAGE', 150, 300),
      (54, 6, 1, 1.02, 2, 3, NULL, TIMESTAMP '2026-08-02 16:51:58.09591', 37, TIMESTAMP '2026-08-02 16:51:58.09591', 'Soy Milk', 'BEVERAGE', 150, 400);

-- ---------------------------------------------------------------------
-- Food tags (15 filas)
-- ---------------------------------------------------------------------
INSERT INTO food_tags (food_id, tag) VALUES
                                         (1, 'MEAT'),
                                         (2, 'FISH'),
                                         (4, 'MEAT'),
                                         (5, 'SOY'),
                                         (7, 'LACTOSE'),
                                         (8, 'LACTOSE'),
                                         (10, 'LACTOSE'),
                                         (11, 'GLUTEN'),
                                         (13, 'GLUTEN'),
                                         (15, 'GLUTEN'),
                                         (28, 'NUTS'),
                                         (29, 'NUTS'),
                                         (31, 'GLUTEN'),
                                         (31, 'HONEY'),
                                         (37, 'SOY');

-- ---------------------------------------------------------------------
-- Food suitable for (90 filas)
-- ---------------------------------------------------------------------
INSERT INTO food_suitable_for (food_id, meal_type) VALUES
                                                       (1, 'DINNER'), (1, 'LUNCH'),
                                                       (2, 'DINNER'), (2, 'LUNCH'),
                                                       (3, 'DINNER'), (3, 'LUNCH'),
                                                       (4, 'DINNER'), (4, 'LUNCH'),
                                                       (5, 'DINNER'), (5, 'LUNCH'),
                                                       (6, 'BREAKFAST'), (6, 'LUNCH'),
                                                       (7, 'BREAKFAST'), (7, 'SNACK'), (7, 'MID_MORNING'),
                                                       (8, 'BREAKFAST'), (8, 'SNACK'), (8, 'MID_MORNING'),
                                                       (9, 'BREAKFAST'), (9, 'SNACK'), (9, 'MID_MORNING'),
                                                       (10, 'BREAKFAST'), (10, 'SNACK'),
                                                       (11, 'BREAKFAST'),
                                                       (12, 'DINNER'), (12, 'LUNCH'),
                                                       (13, 'DINNER'), (13, 'LUNCH'),
                                                       (14, 'DINNER'), (14, 'LUNCH'),
                                                       (15, 'BREAKFAST'), (15, 'SNACK'),
                                                       (16, 'DINNER'), (16, 'LUNCH'),
                                                       (17, 'DINNER'), (17, 'LUNCH'),
                                                       (18, 'DINNER'), (18, 'LUNCH'),
                                                       (19, 'DINNER'), (19, 'LUNCH'), (19, 'BREAKFAST'),
                                                       (20, 'DINNER'), (20, 'LUNCH'), (20, 'SNACK'),
                                                       (21, 'DINNER'), (21, 'LUNCH'),
                                                       (22, 'DINNER'), (22, 'LUNCH'),
                                                       (23, 'DINNER'), (23, 'BREAKFAST'), (23, 'SNACK'), (23, 'MID_MORNING'),
                                                       (24, 'DINNER'), (24, 'BREAKFAST'), (24, 'SNACK'), (24, 'MID_MORNING'),
                                                       (25, 'DINNER'), (25, 'BREAKFAST'), (25, 'SNACK'), (25, 'MID_MORNING'),
                                                       (26, 'DINNER'), (26, 'BREAKFAST'), (26, 'SNACK'), (26, 'MID_MORNING'),
                                                       (27, 'BREAKFAST'), (27, 'LUNCH'),
                                                       (28, 'SNACK'),
                                                       (29, 'BREAKFAST'), (29, 'SNACK'),
                                                       (30, 'DINNER'), (30, 'LUNCH'),
                                                       (31, 'LUNCH'), (31, 'BREAKFAST'), (31, 'SNACK'),
                                                       (32, 'DINNER'), (32, 'LUNCH'), (32, 'SNACK'),
                                                       (33, 'DINNER'), (33, 'LUNCH'),
                                                       (34, 'DINNER'), (34, 'LUNCH'), (34, 'BREAKFAST'), (34, 'SNACK'), (34, 'MID_MORNING'),
                                                       (35, 'BREAKFAST'), (35, 'SNACK'), (35, 'MID_MORNING'),
                                                       (36, 'BREAKFAST'),
                                                       (37, 'BREAKFAST'), (37, 'SNACK');

-- ---------------------------------------------------------------------
-- Nutrition plan templates (5 filas)
-- ---------------------------------------------------------------------
INSERT INTO nutrition_plan_template (
    carb_percentage, fat_percentage, protein_percentage,
    created_at, id, updated_at, created_by, description, name,
    updated_by, target_goal
) VALUES
      (35, 25, 40, TIMESTAMP '2026-08-02 16:51:58.167241', 1, TIMESTAMP '2026-08-02 16:51:58.167241', 'system', 'High protein, moderate carbs, low fat plan for gradual weight loss.', 'Weight Loss - Standard', 'system', 'WEIGHT_LOSS'),
      (40, 15, 45, TIMESTAMP '2026-08-02 16:51:58.167241', 2, TIMESTAMP '2026-08-02 16:51:58.167241', 'system', 'Very high protein intake with complex carbs to support muscle hypertrophy.', 'Muscle Gain - High Protein', 'system', 'MUSCLE_GAIN'),
      (55, 20, 25, TIMESTAMP '2026-08-02 16:51:58.167241', 3, TIMESTAMP '2026-08-02 16:51:58.167241', 'system', 'Plant-based balanced plan excluding all animal products.', 'Vegan - Weight Maintenance', 'system', 'WEIGHT_MAINTENANCE'),
      (40, 30, 30, TIMESTAMP '2026-08-02 16:51:58.168239', 4, TIMESTAMP '2026-08-02 16:51:58.168239', 'system', 'Anti-inflammatory plan free of gluten and lactose.', 'Metabolic Health - Gluten & Lactose Free', 'system', 'METABOLIC_HEALTH'),
      (50, 25, 25, TIMESTAMP '2026-08-02 16:51:58.168239', 5, TIMESTAMP '2026-08-02 16:51:58.168239', 'system', 'Nutrient-dense plan for pregnancy. Avoids alcohol and raw fish.', 'Pregnancy Health - Balanced', 'system', 'PREGNANCY_HEALTH');

-- ---------------------------------------------------------------------
-- Nutrition plan template excluded tags (10 filas)
-- ---------------------------------------------------------------------
INSERT INTO nutrition_plan_template_excluded_tags (nutrition_plan_template_id, food_tag) VALUES
                                                                                             (3, 'MEAT'), (3, 'GELATIN'), (3, 'FISH'), (3, 'EGG'), (3, 'HONEY'), (3, 'LACTOSE'),
                                                                                             (4, 'GLUTEN'), (4, 'LACTOSE'),
                                                                                             (5, 'FISH'), (5, 'ALCOHOL');

-- ---------------------------------------------------------------------
-- Restrictions (5 filas) — se eliminó el segundo INSERT duplicado
-- que reinsertaba los mismos ids 1-5 (violaba la PK en Postgres)
-- ---------------------------------------------------------------------
INSERT INTO restrictions (id, created_at, updated_at, code, name, category, description) VALUES
                                                                                             (1, TIMESTAMP '2026-05-07 14:45:17.371887', TIMESTAMP '2026-05-07 14:45:17.371887', 'GLUTEN_FREE', 'Gluten Free', 'INTOLERANCES', 'Avoid gluten containing foods'),
                                                                                             (2, TIMESTAMP '2026-05-07 14:45:17.384891', TIMESTAMP '2026-05-07 14:45:17.384891', 'LACTOSE_FREE', 'Lactose Free', 'INTOLERANCES', 'Avoid lactose and dairy products'),
                                                                                             (3, TIMESTAMP '2026-05-07 14:45:17.390891', TIMESTAMP '2026-05-07 14:45:17.390891', 'LOW_SODIUM', 'Low Sodium', 'PATHOLOGICAL', 'Reduce sodium intake'),
                                                                                             (4, TIMESTAMP '2026-05-07 14:45:17.404892', TIMESTAMP '2026-05-07 14:45:17.404892', 'VEGAN', 'Vegan', 'DIETARY', 'Avoid all animal products'),
                                                                                             (5, TIMESTAMP '2026-05-07 14:45:17.429902', TIMESTAMP '2026-05-07 14:45:17.429902', 'VEGETARIAN', 'Vegetarian', 'DIETARY', 'Avoid meat and fish');

-- ---------------------------------------------------------------------
-- Users (4 filas) — corregido: password_hash/created_by/role/updated_by
-- estaban desalineados en el INSERT posicional original
-- ---------------------------------------------------------------------
INSERT INTO users (
    birth_date, email_validated, enabled, created_at, id, updated_at,
    dni, first_name, last_name, email, password_hash, created_by, role, updated_by
) VALUES
      (DATE '1990-05-10', FALSE, TRUE, TIMESTAMP '2026-08-02 16:51:57.955872', 1, TIMESTAMP '2026-08-02 16:51:57.955872', '30111222', 'Ana', 'Lopez', 'ana@test.com', '$2a$10$Snpx9uY2JwrDKwV.619M4erTgRGe9MA7yMiMi0Q/rjz.zd3n6ULhK', 'system', 'PATIENT', 'system'),
      (DATE '1985-03-22', FALSE, TRUE, TIMESTAMP '2026-08-02 16:51:57.994523', 2, TIMESTAMP '2026-08-02 16:51:57.994523', '28999111', 'Juan', 'Perez', 'juan@test.com', '$2a$10$OZFFadt4tovt4X.D/BDh8OJSxCDMLUx0BAPVfqlqeJ/YKkAYKosri', 'system', 'PATIENT', 'system'),
      (DATE '2000-01-15', FALSE, TRUE, TIMESTAMP '2026-08-02 16:51:57.996521', 3, TIMESTAMP '2026-08-02 16:51:57.996521', '40123456', 'Maria', 'Gomez', 'maria@test.com', '$2a$10$vGVd3olBpCRbLOK2lzhPjO5LME4vVEaH8AYXIA/v9Dmv3QcOBQQOO', 'system', 'PATIENT', 'system'),
      (NULL, FALSE, TRUE, TIMESTAMP '2026-08-02 16:51:57.999035', 4, TIMESTAMP '2026-08-02 16:51:57.999035', NULL, 'Admin', 'Lirium', 'admin@lirium.com', '$2a$10$8pn4Vs4dQEj714nquwNeku10ATxE89s34jiWpbFDVdRqaML/uj.tG', 'system', 'ADMIN', 'system');

-- ---------------------------------------------------------------------
-- Patient profiles (3 filas) — corregido: activity_level/primary_goal/
-- sex estaban desalineados en el INSERT posicional original
-- ---------------------------------------------------------------------
INSERT INTO patient_profile (
    grams, height, created_at, updated_at, user_id,
    activity_level, created_by, medical_notes, primary_goal, sex, updated_by
) VALUES
      (62000, 163, TIMESTAMP '2026-08-02 16:51:57.987483', TIMESTAMP '2026-08-02 16:51:58.061777', 1, 'ACTIVE', 'system', NULL, 'WEIGHT_MAINTENANCE', 'FEMALE', 'system'),
      (85000, 175, TIMESTAMP '2026-08-02 16:51:57.996521', TIMESTAMP '2026-08-02 16:51:58.062785', 2, 'MODERATE', 'system', NULL, 'WEIGHT_LOSS', 'MALE', 'system'),
      (58000, 160, TIMESTAMP '2026-08-02 16:51:57.999035', TIMESTAMP '2026-08-02 16:51:58.062785', 3, 'VERY_ACTIVE', 'system', NULL, 'MUSCLE_GAIN', 'FEMALE', 'system');

-- ---------------------------------------------------------------------
-- Sincronización de secuencias para todas las tablas con IDs explícitos
-- ---------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('foods', 'id'), (SELECT MAX(id) FROM foods));
SELECT setval(pg_get_serial_sequence('nutrition_plan_template', 'id'), (SELECT MAX(id) FROM nutrition_plan_template));
SELECT setval(pg_get_serial_sequence('restrictions', 'id'), (SELECT MAX(id) FROM restrictions));