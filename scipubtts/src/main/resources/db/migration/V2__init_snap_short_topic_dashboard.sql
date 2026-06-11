-- =========================================================
-- Flyway Migration: V2__create_dashboard_snapshot_tables.sql
-- Purpose: Dashboard snapshot tables
-- Note:
--   V1 already has taxonomy table named "topics".
--   Rename old taxonomy topics to taxonomy_topics first,
--   because dashboard Topic entity uses table name "topics".
-- =========================================================

-- Rename old V1 taxonomy topics table
ALTER TABLE topics RENAME TO taxonomy_topics;

-- Rename old constraints/indexes to avoid confusing names
ALTER TABLE taxonomy_topics
    RENAME CONSTRAINT topics_pkey TO taxonomy_topics_pkey;

ALTER TABLE taxonomy_topics
    RENAME CONSTRAINT fk_topics_subfield_id TO fk_taxonomy_topics_subfield_id;

ALTER INDEX IF EXISTS idx_topics_subfield_id
    RENAME TO idx_taxonomy_topics_subfield_id;


-- =========================================================
-- Publication trends
-- Entity: PublicationTrend
-- =========================================================

CREATE TABLE publication_trends (
                                    publication_year INTEGER PRIMARY KEY,
                                    publication_count BIGINT NOT NULL,

                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- Dashboard topics
-- Entity: Topic
-- Note: field_id will be added in V3
-- =========================================================

CREATE TABLE topics (
                        id BIGSERIAL PRIMARY KEY,

                        topic_id VARCHAR(255) NOT NULL,
                        name VARCHAR(255) NOT NULL,

                        start_time DATE NOT NULL,
                        end_time DATE NOT NULL,

                        velocity DOUBLE PRECISION NOT NULL,
                        acceleration DOUBLE PRECISION NOT NULL,
                        citation_decay DOUBLE PRECISION NOT NULL,
                        newcomer_author DOUBLE PRECISION NOT NULL,
                        institution DOUBLE PRECISION NOT NULL,

                        works BIGINT NOT NULL,
                        citations BIGINT NOT NULL,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT uq_topic_snapshot
                            UNIQUE (topic_id, start_time, end_time)
);


-- =========================================================
-- Metrics
-- Entity: Metric
-- =========================================================

CREATE TABLE metrics (
                         id BIGSERIAL PRIMARY KEY,

                         title VARCHAR(255) NOT NULL,

                         value DOUBLE PRECISION NOT NULL,
                         change_value DOUBLE PRECISION NOT NULL,

                         start_time DATE NOT NULL,
                         end_time DATE NOT NULL,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT uq_metric_snapshot
                             UNIQUE (title, start_time, end_time)
);


-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_topics_topic_id
    ON topics(topic_id);

CREATE INDEX idx_topics_period
    ON topics(start_time, end_time);

CREATE INDEX idx_metrics_period
    ON metrics(start_time, end_time);