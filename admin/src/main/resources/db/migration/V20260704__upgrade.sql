-- 2026-07-04 数据库升级脚本
-- 移除动物园管理员、扫雷、2048 相关能力；扫雷和 2048 当前无数据库对象。

SET NAMES utf8mb4;

DELETE ua
FROM user_achievements ua
JOIN achievements a ON a.id = ua.achievement_id
WHERE a.game_code = 'ZOO';

DELETE FROM achievements
WHERE game_code = 'ZOO';

DROP TABLE IF EXISTS zoo_games;
DROP TABLE IF EXISTS zoo_care_records;

SET @drop_zoo_daily_care_limit = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'zoo_daily_care_limit'
        ),
        'ALTER TABLE users DROP COLUMN zoo_daily_care_limit',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_zoo_daily_care_limit;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS pet_benefit_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    benefit_code VARCHAR(50) NOT NULL,
    benefit_name VARCHAR(100) NOT NULL,
    benefit_type VARCHAR(30) NOT NULL,
    cost_points INT NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    effect_type VARCHAR(50) DEFAULT NULL,
    effect_value INT DEFAULT NULL,
    icon_url VARCHAR(500) DEFAULT NULL,
    stock INT DEFAULT NULL,
    exchange_limit_day INT DEFAULT NULL,
    exchange_limit_total INT DEFAULT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_benefit_code (benefit_code),
    KEY idx_enabled_sort (enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pet_type VARCHAR(50) NOT NULL,
    pet_color_code VARCHAR(50) NOT NULL DEFAULT 'ORANGE',
    pet_asset_key VARCHAR(100) DEFAULT NULL,
    pet_name VARCHAR(50) DEFAULT NULL,
    level INT NOT NULL DEFAULT 1,
    stage_no INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0,
    hunger INT NOT NULL DEFAULT 100,
    clean INT NOT NULL DEFAULT 100,
    happiness INT NOT NULL DEFAULT 100,
    energy INT NOT NULL DEFAULT 100,
    love_value INT NOT NULL DEFAULT 0,
    current_hat_code VARCHAR(50) DEFAULT NULL,
    current_bed_code VARCHAR(50) DEFAULT NULL,
    current_room_theme VARCHAR(50) DEFAULT NULL,
    init_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @add_pet_color_code = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pet_user'
              AND COLUMN_NAME = 'pet_color_code'
        ),
        'SELECT 1',
        'ALTER TABLE pet_user ADD COLUMN pet_color_code VARCHAR(50) NOT NULL DEFAULT ''ORANGE'' AFTER pet_type'
    )
);
PREPARE stmt FROM @add_pet_color_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_pet_asset_key = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pet_user'
              AND COLUMN_NAME = 'pet_asset_key'
        ),
        'SELECT 1',
        'ALTER TABLE pet_user ADD COLUMN pet_asset_key VARCHAR(100) DEFAULT NULL AFTER pet_color_code'
    )
);
PREPARE stmt FROM @add_pet_asset_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_pet_stage_no = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pet_user'
              AND COLUMN_NAME = 'stage_no'
        ),
        'SELECT 1',
        'ALTER TABLE pet_user ADD COLUMN stage_no INT NOT NULL DEFAULT 1 AFTER level'
    )
);
PREPARE stmt FROM @add_pet_stage_no;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_pet_init_time = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pet_user'
              AND COLUMN_NAME = 'init_time'
        ),
        'SELECT 1',
        'ALTER TABLE pet_user ADD COLUMN init_time DATETIME DEFAULT NULL AFTER current_room_theme'
    )
);
PREPARE stmt FROM @add_pet_init_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS pet_type_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_type VARCHAR(50) NOT NULL,
    pet_name VARCHAR(50) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    default_color_code VARCHAR(50) DEFAULT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pet_type (pet_type),
    KEY idx_enabled_sort (enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_color_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_type VARCHAR(50) NOT NULL,
    color_code VARCHAR(50) NOT NULL,
    color_name VARCHAR(50) NOT NULL,
    color_hex VARCHAR(20) DEFAULT NULL,
    asset_key VARCHAR(100) DEFAULT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pet_color (pet_type, color_code),
    KEY idx_pet_type_enabled (pet_type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_growth_stage_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_type VARCHAR(50) NOT NULL,
    color_code VARCHAR(50) NOT NULL,
    stage_no INT NOT NULL,
    stage_name VARCHAR(50) NOT NULL,
    min_level INT NOT NULL,
    max_level INT NOT NULL,
    asset_key VARCHAR(100) NOT NULL,
    preview_asset_key VARCHAR(100) DEFAULT NULL,
    description VARCHAR(500) DEFAULT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pet_color_stage (pet_type, color_code, stage_no),
    KEY idx_pet_color_enabled (pet_type, color_code, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_user_benefit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    benefit_id BIGINT NOT NULL,
    benefit_code VARCHAR(50) NOT NULL,
    benefit_name VARCHAR(100) NOT NULL,
    benefit_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_user_benefit (user_id, benefit_id),
    KEY idx_user_code (user_id, benefit_code),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (benefit_id) REFERENCES pet_benefit_config(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_benefit_exchange_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    benefit_id BIGINT NOT NULL,
    benefit_code VARCHAR(50) NOT NULL,
    benefit_name VARCHAR(100) NOT NULL,
    benefit_type VARCHAR(30) NOT NULL,
    cost_points INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    fail_reason VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_user_create_time (user_id, create_time),
    KEY idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (benefit_id) REFERENCES pet_benefit_config(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_benefit_use_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_benefit_id BIGINT NOT NULL,
    benefit_id BIGINT NOT NULL,
    benefit_code VARCHAR(50) NOT NULL,
    benefit_name VARCHAR(100) NOT NULL,
    benefit_type VARCHAR(30) NOT NULL,
    effect_type VARCHAR(50) DEFAULT NULL,
    effect_value INT DEFAULT NULL,
    effect_desc VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_user_benefit_id (user_benefit_id),
    KEY idx_benefit_id (benefit_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (user_benefit_id) REFERENCES pet_user_benefit(id),
    FOREIGN KEY (benefit_id) REFERENCES pet_benefit_config(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO pet_benefit_config (
    benefit_code, benefit_name, benefit_type, cost_points, description,
    effect_type, effect_value, icon_url, stock, exchange_limit_day, exchange_limit_total,
    enabled, sort_no, create_time, update_time
) VALUES
('PET_MEAL_FULL', '宠物大餐', 'CONSUMABLE', 10, '使用后宠物饥饿值加满', 'HUNGER_FULL', 100, NULL, NULL, 10, NULL, 1, 10, NOW(), NOW()),
('PET_CLEAN_FULL', '超级泡泡浴', 'CONSUMABLE', 10, '使用后宠物清洁值加满', 'CLEAN_FULL', 100, NULL, NULL, 10, NULL, 1, 20, NOW(), NOW()),
('PET_HAPPY_TOY', '快乐玩具包', 'CONSUMABLE', 15, '使用后宠物快乐值加满', 'HAPPINESS_FULL', 100, NULL, NULL, 10, NULL, 1, 30, NOW(), NOW()),
('PET_EXP_FRUIT', '成长果实', 'CONSUMABLE', 20, '使用后宠物成长值增加20', 'EXP_ADD', 20, NULL, NULL, 10, NULL, 1, 40, NOW(), NOW()),
('PET_HAT_BASIC', '小帽子', 'PERMANENT', 30, '兑换后解锁宠物头饰', 'HAT_UNLOCK', NULL, NULL, NULL, 1, 1, 1, 50, NOW(), NOW()),
('PET_BED_BASIC', '小床', 'PERMANENT', 50, '兑换后解锁房间家具', 'BED_UNLOCK', NULL, NULL, NULL, 1, 1, 1, 60, NOW(), NOW()),
('PET_ROOM_FOREST', '森林主题房间', 'PERMANENT', 200, '兑换后永久拥有森林主题房间', 'ROOM_THEME_UNLOCK', NULL, NULL, NULL, 1, 1, 1, 70, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    benefit_name = VALUES(benefit_name),
    benefit_type = VALUES(benefit_type),
    cost_points = VALUES(cost_points),
    description = VALUES(description),
    effect_type = VALUES(effect_type),
    effect_value = VALUES(effect_value),
    stock = VALUES(stock),
    exchange_limit_day = VALUES(exchange_limit_day),
    exchange_limit_total = VALUES(exchange_limit_total),
    enabled = VALUES(enabled),
    sort_no = VALUES(sort_no),
    update_time = NOW();

INSERT INTO pet_type_config
(pet_type, pet_name, description, default_color_code, sort_no, enabled, create_time, update_time)
VALUES
('CAT', '小猫', '可爱又爱干净的小猫', 'ORANGE', 99, 0, NOW(), NOW()),
('DOG', '小狗', '活泼又忠诚的小狗', 'BROWN', 100, 0, NOW(), NOW()),
('RABBIT', '小白兔', '温柔可爱的小白兔', 'WHITE', 1, 1, NOW(), NOW()),
('DINOSAUR', '小恐龙', '勇敢又特别的小恐龙', 'GREEN', 2, 1, NOW(), NOW()),
('ANGELWOMON', '天女兽', '温柔又闪耀的天使系伙伴', 'GOLD', 3, 1, NOW(), NOW()),
('ANGEMON', '天使兽', '正直又可靠的神圣系伙伴', 'GOLD', 4, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    pet_name = VALUES(pet_name),
    description = VALUES(description),
    default_color_code = VALUES(default_color_code),
    sort_no = VALUES(sort_no),
    enabled = VALUES(enabled),
    update_time = NOW();

INSERT INTO pet_color_config
(pet_type, color_code, color_name, color_hex, asset_key, sort_no, enabled, create_time, update_time)
VALUES
('CAT', 'ORANGE', '橘色', '#F6A23A', 'cat_orange', 1, 0, NOW(), NOW()),
('CAT', 'WHITE', '白色', '#FFFFFF', 'cat_white', 2, 0, NOW(), NOW()),
('CAT', 'GRAY', '灰色', '#BFC3C7', 'cat_gray', 3, 0, NOW(), NOW()),
('DOG', 'BROWN', '棕色', '#A66A3F', 'dog_brown', 1, 0, NOW(), NOW()),
('DOG', 'WHITE', '白色', '#FFFFFF', 'dog_white', 2, 0, NOW(), NOW()),
('DOG', 'YELLOW', '黄色', '#F4D03F', 'dog_yellow', 3, 0, NOW(), NOW()),
('RABBIT', 'WHITE', '白色', '#FFFFFF', 'rabbit_white', 1, 1, NOW(), NOW()),
('RABBIT', 'PINK', '粉色', '#FFB6C1', 'rabbit_pink', 2, 1, NOW(), NOW()),
('RABBIT', 'GRAY', '灰色', '#BFC3C7', 'rabbit_gray', 3, 1, NOW(), NOW()),
('DINOSAUR', 'GREEN', '绿色', '#67C23A', 'dinosaur_green', 1, 1, NOW(), NOW()),
('DINOSAUR', 'BLUE', '蓝色', '#409EFF', 'dinosaur_blue', 2, 1, NOW(), NOW()),
('DINOSAUR', 'PURPLE', '紫色', '#9B59B6', 'dinosaur_purple', 3, 1, NOW(), NOW()),
('ANGELWOMON', 'GOLD', '金色', '#F6C85F', 'angelwomon_gold', 1, 1, NOW(), NOW()),
('ANGELWOMON', 'WHITE', '白色', '#FFFFFF', 'angelwomon_white', 2, 1, NOW(), NOW()),
('ANGELWOMON', 'PINK', '粉色', '#FF9ECF', 'angelwomon_pink', 3, 1, NOW(), NOW()),
('ANGEMON', 'GOLD', '金色', '#F6C85F', 'angemon_gold', 1, 1, NOW(), NOW()),
('ANGEMON', 'WHITE', '白色', '#FFFFFF', 'angemon_white', 2, 1, NOW(), NOW()),
('ANGEMON', 'BLUE', '蓝色', '#7DB9FF', 'angemon_blue', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    color_name = VALUES(color_name),
    color_hex = VALUES(color_hex),
    asset_key = VALUES(asset_key),
    sort_no = VALUES(sort_no),
    enabled = VALUES(enabled),
    update_time = NOW();

INSERT INTO pet_growth_stage_config
(pet_type, color_code, stage_no, stage_name, min_level, max_level, asset_key, preview_asset_key, description, enabled, sort_no, create_time, update_time)
SELECT
    c.pet_type,
    c.color_code,
    s.stage_no,
    s.stage_name,
    s.min_level,
    s.max_level,
    CONCAT(c.asset_key, '_stage_', s.stage_no),
    CONCAT(c.asset_key, '_stage_', s.stage_no, '_preview'),
    CASE s.stage_no
        WHEN 1 THEN CONCAT(t.pet_name, '刚来到你身边，正是小小的幼年期')
        WHEN 2 THEN CONCAT(t.pet_name, '进入成长期，变得更加活泼')
        WHEN 3 THEN CONCAT(t.pet_name, '来到进阶期，外观变化更明显')
        WHEN 4 THEN CONCAT(t.pet_name, '进入成熟期，更完整也更有特色')
        ELSE CONCAT(t.pet_name, '成长为完全体，已经是最棒的伙伴')
    END,
    1,
    s.stage_no,
    NOW(),
    NOW()
FROM pet_color_config c
JOIN pet_type_config t ON t.pet_type = c.pet_type
JOIN (
    SELECT 1 AS stage_no, '幼年期' AS stage_name, 1 AS min_level, 5 AS max_level
    UNION ALL SELECT 2, '成长期', 6, 15
    UNION ALL SELECT 3, '进阶期', 16, 30
    UNION ALL SELECT 4, '成熟期', 31, 50
    UNION ALL SELECT 5, '完全体', 51, 70
) s
WHERE c.enabled = 1
ON DUPLICATE KEY UPDATE
    stage_name = VALUES(stage_name),
    min_level = VALUES(min_level),
    max_level = VALUES(max_level),
    asset_key = VALUES(asset_key),
    preview_asset_key = VALUES(preview_asset_key),
    description = VALUES(description),
    enabled = VALUES(enabled),
    sort_no = VALUES(sort_no),
    update_time = NOW();

UPDATE pet_growth_stage_config
SET enabled = 0,
    update_time = NOW()
WHERE pet_type IN ('CAT', 'DOG');

UPDATE pet_user
SET level = 70,
    exp = 0,
    update_time = NOW()
WHERE level > 70;

UPDATE pet_user
SET stage_no = CASE
        WHEN level <= 5 THEN 1
        WHEN level <= 15 THEN 2
        WHEN level <= 30 THEN 3
        WHEN level <= 50 THEN 4
        ELSE 5
    END,
    update_time = NOW();

UPDATE pet_user p
LEFT JOIN pet_growth_stage_config s
  ON s.pet_type = p.pet_type
 AND s.color_code = p.pet_color_code
 AND s.stage_no = p.stage_no
SET p.pet_asset_key = COALESCE(s.asset_key, p.pet_asset_key),
    p.init_time = COALESCE(p.init_time, p.create_time)
WHERE s.asset_key IS NOT NULL
   OR p.pet_asset_key IS NULL
   OR p.init_time IS NULL;
