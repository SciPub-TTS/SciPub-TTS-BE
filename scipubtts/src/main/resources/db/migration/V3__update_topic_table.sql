-- =========================================================
-- Flyway Migration: V3__add_field_id_to_topics.sql
-- Purpose: Add field_id to dashboard topics table
-- Entity: Topic.fieldId
-- =========================================================

ALTER TABLE topics
    ADD COLUMN field_id INTEGER;

-- Safety for existing rows if any data was inserted before this migration
UPDATE topics
SET field_id = 0
WHERE field_id IS NULL;

ALTER TABLE topics
    ALTER COLUMN field_id SET NOT NULL;

CREATE INDEX idx_topics_field_id
    ON topics(field_id);