-- ============================================================
-- V1: Create users table for Auth Service
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(30)     NOT NULL,
    email       VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    full_name   VARCHAR(100),
    role        VARCHAR(20)     NOT NULL DEFAULT 'USER',
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

-- ── Performance Indexes ─────────────────────────────────────
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_role     ON users (role);
