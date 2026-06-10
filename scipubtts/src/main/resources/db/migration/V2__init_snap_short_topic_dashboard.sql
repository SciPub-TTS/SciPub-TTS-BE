CREATE TABLE publication_trends (
                                    publication_year INTEGER PRIMARY KEY,
                                    publication_count BIGINT NOT NULL,

                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE topics (
                        id BIGSERIAL PRIMARY KEY,

                        topic_id VARCHAR(255) NOT NULL,
                        name VARCHAR(500) NOT NULL,

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

CREATE INDEX idx_topics_topic_id
    ON topics(topic_id);

CREATE INDEX idx_topics_period
    ON topics(start_time, end_time);

CREATE INDEX idx_metrics_period
    ON metrics(start_time, end_time);