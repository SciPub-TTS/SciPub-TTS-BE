    -- ====================================================================
-- Version: V19
-- Description: Update research_feed_item structure to support rich card display
--              and clean up unused fields (relevance_score, is_seen).
-- ====================================================================

ALTER TABLE research_feed_item DROP COLUMN IF EXISTS relevance_score;
ALTER TABLE research_feed_item DROP COLUMN IF EXISTS is_seen;

ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS author_openalex_ids_snapshot TEXT;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS work_type_snapshot TEXT;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS topic_snapshot TEXT;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS topic_openalex_id_snapshot TEXT;

ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS abstract_text TEXT;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS doi TEXT;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS pdf_url TEXT;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS keywords_json jsonb;
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS primary_fields_snapshot VARCHAR(255);
ALTER TABLE research_feed_item ADD COLUMN IF NOT EXISTS subfield_snapshot VARCHAR(255);
