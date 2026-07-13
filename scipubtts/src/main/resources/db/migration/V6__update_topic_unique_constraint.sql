-- Allow same topic snapshot for different field_id values.
ALTER TABLE topics
    DROP CONSTRAINT uq_topic_snapshot;

ALTER TABLE topics
    ADD CONSTRAINT uq_topic_snapshot
        UNIQUE (topic_id, start_time, end_time, field_id);