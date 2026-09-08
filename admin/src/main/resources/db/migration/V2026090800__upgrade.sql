CREATE TABLE admin_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    token_version INT NOT NULL DEFAULT 0,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_account_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_user_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    bound_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_user_relation_user (user_id),
    KEY idx_admin_user_relation_admin (admin_id),
    CONSTRAINT fk_admin_user_relation_admin
        FOREIGN KEY (admin_id) REFERENCES admin_account (id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_user_relation_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    module VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    before_data LONGTEXT NULL,
    after_data LONGTEXT NULL,
    request_id VARCHAR(64) NULL,
    ip_address VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin_operation_log_admin_time (admin_id, created_at),
    KEY idx_admin_operation_log_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_english_user_config (
    user_id BIGINT PRIMARY KEY,
    grade_level TINYINT NOT NULL,
    skill_markdown TEXT NULL,
    updated_by_admin_id BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_daily_english_user_config_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_daily_english_user_config_admin
        FOREIGN KEY (updated_by_admin_id) REFERENCES admin_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS admin_portal_config;
