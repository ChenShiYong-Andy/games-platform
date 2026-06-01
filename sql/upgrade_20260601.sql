-- 2026-06-01 数据库升级脚本
-- 线上数据库已执行过 init.sql，请勿重复执行初始化脚本。

SET NAMES utf8mb4;

USE games_platform;

ALTER TABLE users
    ADD COLUMN sudoku_daily_limit INT NOT NULL DEFAULT 5 AFTER total_clears,
    ADD COLUMN zoo_daily_care_limit INT NOT NULL DEFAULT 10 AFTER sudoku_daily_limit;

CREATE TABLE zoo_care_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cared_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_cared_at (user_id, cared_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE zoo_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    current_need VARCHAR(20) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    points_awarded INT NOT NULL DEFAULT 0,
    points_deducted INT NOT NULL DEFAULT 0,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME DEFAULT NULL,
    INDEX idx_user_status (user_id, status),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE achievements
    ADD COLUMN game_code VARCHAR(50) NOT NULL DEFAULT 'SUDOKU' AFTER id,
    DROP INDEX code,
    ADD UNIQUE KEY uk_game_achievement_code (game_code, code);

UPDATE achievements
SET game_code = 'PLATFORM'
WHERE code IN ('LOGIN_STREAK_3', 'LOGIN_STREAK_7');

INSERT INTO achievements (game_code, code, name, description, icon) VALUES
('ZOO', 'FIRST_CARE', '爱心启程', '首次成功照料动物园伙伴', '🌱'),
('ZOO', 'CARE_10', '贴心管理员', '累计成功照料10次', '💚'),
('ZOO', 'CARE_50', '动物园之星', '累计成功照料50次', '⭐'),
('ZOO', 'CARE_100', '金牌管理员', '累计成功照料100次', '🏆');
