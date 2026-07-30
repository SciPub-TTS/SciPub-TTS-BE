ALTER TABLE api_call_log
    ADD COLUMN IF NOT EXISTS caller_type VARCHAR(20);

ALTER TABLE api_call_log
    DROP CONSTRAINT IF EXISTS chk_api_call_log_registered_user_only;

ALTER TABLE api_call_log
    DROP CONSTRAINT IF EXISTS chk_api_call_log_caller_identity;

UPDATE api_call_log
SET caller_type = CASE
    WHEN user_id IS NOT NULL THEN 'USER'
    WHEN job_id IS NOT NULL THEN 'SYSTEM'
    ELSE 'GUEST'
END
WHERE caller_type IS NULL
   OR caller_type NOT IN ('USER', 'GUEST', 'SYSTEM')
   OR (caller_type = 'SYSTEM' AND job_id IS NULL)
   OR (caller_type = 'SYSTEM' AND user_id IS NOT NULL)
   OR (caller_type = 'USER' AND user_id IS NULL)
   OR (caller_type = 'GUEST' AND (user_id IS NOT NULL OR job_id IS NOT NULL));

UPDATE api_call_log
SET user_id = NULL
WHERE caller_type IN ('GUEST', 'SYSTEM');

UPDATE api_call_log
SET job_id = NULL
WHERE caller_type = 'GUEST';

ALTER TABLE api_call_log
    ALTER COLUMN caller_type SET NOT NULL;

ALTER TABLE api_call_log
    ADD CONSTRAINT chk_api_call_log_caller_identity
        CHECK (
            (caller_type = 'USER' AND user_id IS NOT NULL)
            OR (caller_type = 'GUEST' AND user_id IS NULL AND job_id IS NULL)
            OR (caller_type = 'SYSTEM' AND user_id IS NULL)
        );
