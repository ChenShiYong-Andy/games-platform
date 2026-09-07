CREATE TABLE admin_portal_config (
    id BIGINT NOT NULL PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_english_config (
    id BIGINT NOT NULL PRIMARY KEY,
    grade_level TINYINT NOT NULL DEFAULT 1,
    skill_markdown TEXT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO daily_english_config (id, grade_level, skill_markdown)
VALUES (1, 1, '');

CREATE TABLE daily_english_practice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    practice_date DATE NOT NULL,
    grade_level TINYINT NOT NULL,
    content_json LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_daily_english_date_grade (practice_date, grade_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @drop_user_admin_password = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'admin_password_hash'
        ),
        'ALTER TABLE users DROP COLUMN admin_password_hash',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_user_admin_password;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_sudoku_daily_limit = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'users'
              AND COLUMN_NAME = 'sudoku_daily_limit'
        ),
        'ALTER TABLE users DROP COLUMN sudoku_daily_limit',
        'SELECT 1'
    )
);

PREPARE stmt FROM @drop_sudoku_daily_limit;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

