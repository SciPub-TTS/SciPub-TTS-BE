-- =============================================================================
-- V10 — Schema for User Profile + Social Hub + User Bookmark
-- =============================================================================
ALTER TABLE api_job
    ADD COLUMN IF NOT EXISTS request_params TEXT;


ALTER TABLE api_job ADD COLUMN total_failed INT;