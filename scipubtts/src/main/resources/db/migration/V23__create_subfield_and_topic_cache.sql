CREATE TABLE total_subfield
(
    id             BIGSERIAL PRIMARY KEY,
    openalex_id    VARCHAR(100) NOT NULL UNIQUE,
    display_name   TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE total_topic
(
    id             BIGSERIAL PRIMARY KEY,
    openalex_id    VARCHAR(100) NOT NULL UNIQUE,
    display_name   TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_total_subfield_openalex_id ON total_subfield (openalex_id);
CREATE INDEX idx_total_topic_openalex_id ON total_topic (openalex_id);

INSERT INTO system_values (config_key, config_value, description)
VALUES ('cron.schedule.openalex_field_taxonomy.sync_monthly',
        '0 0 3 1 * *',
        'Schedule for monthly OpenAlex field taxonomy synchronization job (Every month on day 1 at 03:00 AM)')
ON CONFLICT (config_key) DO NOTHING;
