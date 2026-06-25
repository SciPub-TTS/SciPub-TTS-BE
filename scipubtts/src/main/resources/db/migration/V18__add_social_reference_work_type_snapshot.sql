ALTER TABLE social_post_reference
    ADD COLUMN IF NOT EXISTS work_type_snapshot TEXT;

UPDATE social_post_reference spr
SET work_type_snapshot = ub.work_type_snapshot
FROM social_post sp
         JOIN user_bookmark ub ON ub.user_id = sp.author_id
WHERE spr.post_id = sp.id
  AND ub.openalex_id = spr.openalex_id
  AND (spr.work_type_snapshot IS NULL OR btrim(spr.work_type_snapshot) = '')
  AND ub.work_type_snapshot IS NOT NULL
  AND btrim(ub.work_type_snapshot) <> '';
