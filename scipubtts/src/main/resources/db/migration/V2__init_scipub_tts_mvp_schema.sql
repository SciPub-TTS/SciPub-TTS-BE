-- =========================================================
-- Flyway Migration: V2__init_scipub_tts_mvp_schema.sql
-- Purpose: User features, OpenAlex JSONB caching, and background job logs for SciPub-TTS MVP
-- Database: PostgreSQL
-- =========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- 1. USERS / AUTH
-- =========================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255),

    password_hash VARCHAR(255),

    first_name VARCHAR(255),
    last_name VARCHAR(255),

    role VARCHAR(50) NOT NULL DEFAULT 'RESEARCHER',

    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_google_authenticated BOOLEAN NOT NULL DEFAULT FALSE,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_users_role CHECK (role IN ('RESEARCHER', 'ADMIN'))
);

CREATE TABLE email_verification_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    token VARCHAR(255) NOT NULL UNIQUE,

    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    redirect_url VARCHAR(1000)
);

CREATE TABLE password_reset_challenge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    email_snapshot VARCHAR(255) NOT NULL,

    code_hash VARCHAR(255) NOT NULL,

    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,

    verified_at TIMESTAMPTZ,
    invalidated_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE password_reset_grant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    challenge_id UUID NOT NULL REFERENCES password_reset_challenge(id) ON DELETE CASCADE,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    expired_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    remember_me BOOLEAN NOT NULL DEFAULT FALSE,

    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expired_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

-- =========================================================
-- 2. IAM / USER FEATURES
-- =========================================================

CREATE TABLE user_bookmark (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    entity_type VARCHAR(30) NOT NULL,
    openalex_id TEXT NOT NULL,

    title_snapshot TEXT,
    authors_snapshot TEXT,
    source_snapshot TEXT,
    topic_snapshot TEXT,

    publication_year INT,
    citation_snapshot INT,

    note TEXT,

    share_token VARCHAR(255),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE(user_id, entity_type, openalex_id),
    CONSTRAINT chk_user_bookmark_entity_type CHECK (entity_type IN ('WORK', 'AUTHOR', 'TOPIC', 'SOURCE', 'INSTITUTION'))
);

CREATE TABLE user_follow (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    target_type VARCHAR(30) NOT NULL,
    target_openalex_id TEXT NOT NULL,

    display_name_snapshot TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE(user_id, target_type, target_openalex_id),
    CONSTRAINT chk_user_follow_target_type CHECK (target_type IN ('AUTHOR', 'TOPIC', 'SOURCE'))
);

CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID REFERENCES users(id) ON DELETE SET NULL,

    query_text TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE research_feed_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    work_openalex_id TEXT NOT NULL,

    title_snapshot TEXT,
    authors_snapshot TEXT,
    source_snapshot TEXT,
    publication_year INT,
    citation_snapshot INT,

    generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================================================
-- 3. OPENALEX ENTITY CACHE
-- =========================================================

CREATE TABLE openalex_entity_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    entity_type VARCHAR(30) NOT NULL,
    openalex_id TEXT NOT NULL,

    raw_payload JSONB NOT NULL,

    cached_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,

    UNIQUE(entity_type, openalex_id),
    CONSTRAINT chk_openalex_cache_entity_type CHECK (entity_type IN ('WORK', 'AUTHOR', 'INSTITUTION', 'SOURCE'))
);

-- =========================================================
-- 4. SYNC / CRONJOB LOG
-- =========================================================

CREATE TABLE api_job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    total_fetched INT DEFAULT 0,
    total_saved INT DEFAULT 0,

    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,

    error_log TEXT,

    CONSTRAINT chk_api_job_type CHECK (job_type IN ('DASHBOARD_SYNC', 'FEED_SYNC')),
    CONSTRAINT chk_api_job_status CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED'))
);

CREATE TABLE api_call_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    job_id UUID REFERENCES api_job(id) ON DELETE CASCADE,

    endpoint TEXT NOT NULL,
    query_params TEXT,

    response_status INT,

    records_fetched INT DEFAULT 0,

    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,

    error_log TEXT
);

-- =========================================================
-- 5. INDEXES
-- =========================================================

CREATE INDEX idx_email_verification_user ON email_verification_token(user_id);
CREATE INDEX idx_email_verification_token ON email_verification_token(token);

CREATE INDEX idx_password_reset_challenge_user ON password_reset_challenge(user_id);
CREATE INDEX idx_password_reset_grant_user ON password_reset_grant(user_id);
CREATE INDEX idx_password_reset_grant_challenge ON password_reset_grant(challenge_id);

CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);

CREATE INDEX idx_bookmark_user ON user_bookmark(user_id);
CREATE INDEX idx_bookmark_openalex ON user_bookmark(entity_type, openalex_id);

CREATE INDEX idx_follow_user ON user_follow(user_id);
CREATE INDEX idx_follow_target ON user_follow(target_type, target_openalex_id);

CREATE INDEX idx_search_history_user_created ON search_history(user_id, created_at DESC);

CREATE INDEX idx_feed_user_generated ON research_feed_item(user_id, generated_at DESC);

CREATE INDEX idx_openalex_cache_openalex_id ON openalex_entity_cache(openalex_id);
CREATE INDEX idx_openalex_cache_entity ON openalex_entity_cache(entity_type, openalex_id);
CREATE INDEX idx_openalex_cache_expires ON openalex_entity_cache(expires_at);
CREATE INDEX idx_openalex_cache_raw_payload ON openalex_entity_cache USING GIN (raw_payload);

CREATE INDEX idx_api_job_status ON api_job(status);
CREATE INDEX idx_api_call_log_job ON api_call_log(job_id);
