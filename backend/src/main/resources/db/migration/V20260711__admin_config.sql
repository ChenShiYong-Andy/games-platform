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
