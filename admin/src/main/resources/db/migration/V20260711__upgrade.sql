-- 2026-07-11 管理员配置与宠物初始状态调整

SET NAMES utf8mb4;

SET @add_admin_password_hash = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'admin_password_hash'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN admin_password_hash VARCHAR(255) DEFAULT NULL AFTER password_hash'
    )
);
PREPARE stmt FROM @add_admin_password_hash;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE pet_user
    MODIFY COLUMN hunger INT NOT NULL DEFAULT 50,
    MODIFY COLUMN clean INT NOT NULL DEFAULT 50,
    MODIFY COLUMN happiness INT NOT NULL DEFAULT 50,
    MODIFY COLUMN energy INT NOT NULL DEFAULT 50;

UPDATE users
SET sudoku_daily_limit = 50,
    updated_at = NOW()
WHERE sudoku_daily_limit > 50;


-- 2026-07-11 宠物权益兑换数量

SET NAMES utf8mb4;

SET @add_pet_exchange_quantity = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pet_benefit_exchange_order'
              AND COLUMN_NAME = 'quantity'
        ),
        'SELECT 1',
        'ALTER TABLE pet_benefit_exchange_order ADD COLUMN quantity INT NOT NULL DEFAULT 1 AFTER cost_points'
    )
);
PREPARE stmt FROM @add_pet_exchange_quantity;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO pet_benefit_config (
    benefit_code, benefit_name, benefit_type, cost_points, description,
    effect_type, effect_value, icon_url, stock, exchange_limit_day, exchange_limit_total,
    enabled, sort_no, create_time, update_time
) VALUES
    ('PET_ENERGY_DRINK', '活力饮料', 'CONSUMABLE', 10, '使用后宠物体力值增加5%', 'ENERGY_FULL', 5, NULL, NULL, NULL, NULL, 1, 35, NOW(), NOW())
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

UPDATE pet_benefit_config
SET enabled = 0,
    exchange_limit_day = NULL,
    exchange_limit_total = NULL,
    update_time = NOW()
WHERE benefit_code = 'PET_EXP_FRUIT';

UPDATE pet_benefit_config
SET description = '使用后宠物饥饿值增加5%',
    effect_value = 5,
    update_time = NOW()
WHERE benefit_code = 'PET_MEAL_FULL';

UPDATE pet_benefit_config
SET description = '使用后宠物清洁值增加5%',
    effect_value = 5,
    update_time = NOW()
WHERE benefit_code = 'PET_CLEAN_FULL';

UPDATE pet_benefit_config
SET description = '使用后宠物快乐值增加10%',
    effect_value = 10,
    update_time = NOW()
WHERE benefit_code = 'PET_HAPPY_TOY';

UPDATE pet_benefit_config
SET exchange_limit_day = NULL,
    exchange_limit_total = NULL,
    update_time = NOW()
WHERE benefit_type = 'CONSUMABLE';
