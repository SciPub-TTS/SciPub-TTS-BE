ALTER TABLE user_bookmark
    ADD COLUMN IF NOT EXISTS author_openalex_ids_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS topic_openalex_id_snapshot TEXT;

ALTER TABLE social_post_reference
    ADD COLUMN IF NOT EXISTS author_openalex_ids_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS topic_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS topic_openalex_id_snapshot TEXT;
