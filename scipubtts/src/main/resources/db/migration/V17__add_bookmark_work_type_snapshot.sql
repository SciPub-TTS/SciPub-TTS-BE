ALTER TABLE user_bookmark
    ADD COLUMN IF NOT EXISTS work_type_snapshot TEXT;
