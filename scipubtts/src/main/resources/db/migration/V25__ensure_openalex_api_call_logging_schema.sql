ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS caller_type VARCHAR(20);

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS user_id UUID;

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS job_type VARCHAR(80);

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS method VARCHAR(10);

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS duration_ms BIGINT;

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS query_params TEXT;

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ;

ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS error_log TEXT;

ALTER TABLE api_call_log
    ALTER COLUMN records_fetched DROP DEFAULT;

ALTER TABLE api_call_log
    ALTER COLUMN records_fetched DROP NOT NULL;

DO
$$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conrelid = 'api_call_log'::regclass
              AND contype = 'f'
              AND conname = 'fk_api_call_log_user'
        ) THEN
            ALTER TABLE api_call_log
                ADD CONSTRAINT fk_api_call_log_user
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
        END IF;
    END
$$;

DO
$$
    DECLARE
        constraint_name TEXT;
        desired_exists BOOLEAN;
    BEGIN
        SELECT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conrelid = 'api_call_log'::regclass
              AND contype = 'f'
              AND conname = 'fk_api_call_log_job'
              AND pg_get_constraintdef(oid) LIKE '%ON DELETE SET NULL%'
        )
        INTO desired_exists;

        IF NOT desired_exists THEN
            SELECT conname
            INTO constraint_name
            FROM pg_constraint
            WHERE conrelid = 'api_call_log'::regclass
              AND contype = 'f'
              AND pg_get_constraintdef(oid) LIKE '%REFERENCES api_job%'
            LIMIT 1;

            IF constraint_name IS NOT NULL THEN
                EXECUTE format('ALTER TABLE api_call_log DROP CONSTRAINT %I', constraint_name);
            END IF;

            ALTER TABLE api_call_log
                ADD CONSTRAINT fk_api_call_log_job
                    FOREIGN KEY (job_id) REFERENCES api_job(id) ON DELETE SET NULL;
        END IF;
    END
$$;

DO
$$
    DECLARE
        constraint_name TEXT;
    BEGIN
        SELECT conname
        INTO constraint_name
        FROM pg_constraint
        WHERE conrelid = 'api_job'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) LIKE '%status%'
        LIMIT 1;

        IF constraint_name IS NOT NULL THEN
            EXECUTE format('ALTER TABLE api_job DROP CONSTRAINT %I', constraint_name);
        END IF;

        ALTER TABLE api_job
            ADD CONSTRAINT chk_api_job_status
                CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'));
    END
$$;

CREATE INDEX IF NOT EXISTS idx_api_call_log_started_id
    ON api_call_log (started_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_api_call_log_user
    ON api_call_log (user_id);

CREATE INDEX IF NOT EXISTS idx_api_call_log_caller_type
    ON api_call_log (caller_type);

CREATE INDEX IF NOT EXISTS idx_api_call_log_job_type
    ON api_call_log (job_type);

CREATE INDEX IF NOT EXISTS idx_api_call_log_response_status
    ON api_call_log (response_status);

CREATE INDEX IF NOT EXISTS idx_api_call_log_endpoint
    ON api_call_log (endpoint);
