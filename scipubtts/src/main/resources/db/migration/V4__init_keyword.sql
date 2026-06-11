-- =========================================================
-- Flyway Migration: V4__create_keywords.sql
-- Purpose: Create keywords dashboard snapshot table
-- Entity: Keyword
-- =========================================================

CREATE TABLE keywords (
                          id BIGSERIAL PRIMARY KEY,

                          keyword_id VARCHAR(500) NOT NULL,
                          keyword VARCHAR(500) NOT NULL,

                          field_id VARCHAR(100) NOT NULL,

                          start_time DATE NOT NULL,
                          end_time DATE NOT NULL,

    -- Metric Group 1: Publication Growth Rate
                          pgr DOUBLE PRECISION,
                          cagr DOUBLE PRECISION,

    -- Metric Group 2: Publication Share
                          ps DOUBLE PRECISION,

    -- Raw counts
                          works_count BIGINT NOT NULL,
                          cited_by_count BIGINT NOT NULL,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT uq_keyword_snapshot
                              UNIQUE (keyword_id, field_id, start_time, end_time)
);

CREATE INDEX idx_keywords_keyword
    ON keywords(keyword);

CREATE INDEX idx_keywords_field
    ON keywords(field_id);

CREATE INDEX idx_keywords_period
    ON keywords(start_time, end_time);