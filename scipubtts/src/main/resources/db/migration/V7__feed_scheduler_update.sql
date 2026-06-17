-- =========================================================
-- Flyway Migration: V7__feed_scheduler_update.sql
-- Purpose: New Feed Scheduler support
-- Database: PostgreSQL
-- =========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- 1. Ensure api_job exists
-- =========================================================

CREATE TABLE IF NOT EXISTS api_job
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    job_type       VARCHAR(50) NOT NULL,
    status         VARCHAR(50) NOT NULL,

    request_params JSONB,

    total_fetched  INT              DEFAULT 0,
    total_saved    INT              DEFAULT 0,
    total_failed   INT              DEFAULT 0,

    started_at     TIMESTAMPTZ,
    finished_at    TIMESTAMPTZ,

    error_log      TEXT
);

CREATE INDEX IF NOT EXISTS idx_api_job_type_status_finished
    ON api_job (job_type, status, finished_at DESC);

-- =========================================================
-- 2. user_follow: only TOPIC / AUTHOR for MVP feed
-- =========================================================

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'chk_user_follow_target_type') THEN
            ALTER TABLE user_follow
                ADD CONSTRAINT chk_user_follow_target_type
                    CHECK (target_type IN ('TOPIC', 'AUTHOR')) NOT VALID;
        END IF;
    END
$$;

CREATE INDEX IF NOT EXISTS idx_user_follow_target
    ON user_follow (target_type, target_openalex_id);

CREATE INDEX IF NOT EXISTS idx_user_follow_user
    ON user_follow (user_id);

-- =========================================================
-- 3. research_feed_item: add scheduler/feed fields
-- =========================================================

ALTER TABLE research_feed_item
    ADD COLUMN IF NOT EXISTS publication_date DATE;

ALTER TABLE research_feed_item
    ADD COLUMN IF NOT EXISTS reason_json JSONB DEFAULT '{
      "reasons": []
    }'::jsonb;

ALTER TABLE research_feed_item
    ADD COLUMN IF NOT EXISTS relevance_score DOUBLE PRECISION;

ALTER TABLE research_feed_item
    ADD COLUMN IF NOT EXISTS is_seen BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE research_feed_item
    ADD COLUMN IF NOT EXISTS dismissed_at TIMESTAMPTZ;

-- Remove duplicate feed rows before adding unique constraint.
WITH ranked AS (SELECT id,
                       ROW_NUMBER() OVER (
                           PARTITION BY user_id, work_openalex_id
                           ORDER BY generated_at DESC, id DESC
                           ) AS rn
                FROM research_feed_item)
DELETE
FROM research_feed_item r
    USING ranked d
WHERE r.id = d.id
  AND d.rn > 1;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'uq_research_feed_user_work') THEN
            ALTER TABLE research_feed_item
                ADD CONSTRAINT uq_research_feed_user_work
                    UNIQUE (user_id, work_openalex_id);
        END IF;
    END
$$;

CREATE INDEX IF NOT EXISTS idx_research_feed_user_generated
    ON research_feed_item (user_id, generated_at DESC);

CREATE INDEX IF NOT EXISTS idx_research_feed_user_seen
    ON research_feed_item (user_id, is_seen);

CREATE INDEX IF NOT EXISTS idx_research_feed_work
    ON research_feed_item (work_openalex_id);