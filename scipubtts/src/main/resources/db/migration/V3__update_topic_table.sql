ALTER TABLE topics
    ADD COLUMN field_id INTEGER NOT NULL;

CREATE INDEX idx_topics_field_id
    ON topics(field_id);