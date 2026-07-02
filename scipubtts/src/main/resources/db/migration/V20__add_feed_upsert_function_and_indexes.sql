-- ==========================================
-- V20__add_feed_upsert_function_and_indexes.sql
-- ==========================================

-- 1. FIX LỖI 42P10 CHO BẢNG search_history
ALTER TABLE public.search_history
DROP CONSTRAINT IF EXISTS unq_search_history_conflict;

ALTER TABLE public.search_history
    ADD CONSTRAINT unq_search_history_conflict UNIQUE (user_id, query_text, created_at);

-- 2. ĐẢM BẢO UNIQUE CONSTRAINT CHO BẢNG research_feed_item
-- Để lệnh ON CONFLICT dưới đây không bị văng lỗi 42P10
ALTER TABLE public.research_feed_item
DROP CONSTRAINT IF EXISTS unq_research_feed_item_conflict;

ALTER TABLE public.research_feed_item
    ADD CONSTRAINT unq_research_feed_item_conflict UNIQUE (user_id, work_openalex_id);

-- 3. TẠO INDEX TỐI ƯU TRUY VẤN
CREATE INDEX IF NOT EXISTS idx_research_feed_item_user_id ON public.research_feed_item(user_id);
CREATE INDEX IF NOT EXISTS idx_research_feed_item_pub_date ON public.research_feed_item(publication_date DESC);

-- 4. TẠO FUNCTION XỬ LÝ UPSERT HÀNG LOẠT (PUSH LOGIC TO DATA)
CREATE OR REPLACE FUNCTION public.batch_upsert_research_feed(p_drafts_json JSONB)
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
v_affected_rows integer;
BEGIN
INSERT INTO public.research_feed_item (
    id, user_id, work_openalex_id, title_snapshot, authors_snapshot, source_snapshot,
    publication_year, publication_date, citation_snapshot, reason_json, generated_at,
    author_openalex_ids_snapshot, work_type_snapshot, topic_snapshot, topic_openalex_id_snapshot,
    abstract_text, doi, pdf_url, keywords_json, primary_fields_snapshot, subfield_snapshot
)
SELECT
    COALESCE((x.draft->>'id')::uuid, gen_random_uuid()), -- Tự gen UUID nếu BE không truyền
    (x.draft->>'userId')::uuid,
    x.draft->>'workOpenalexId',
    x.draft->>'titleSnapshot',
    x.draft->>'authorsSnapshot',
    x.draft->>'sourceSnapshot',
    (x.draft->>'publicationYear')::integer,
    (x.draft->>'publicationDate')::date,
    (x.draft->>'citationSnapshot')::integer,
    (x.draft->>'reasonsJson')::jsonb, -- BE chỉ cần truyền format json chuẩn
    (x.draft->>'generatedAt')::timestamp,
    x.draft->>'authorOpenAlexIdsSnapshot',
    x.draft->>'workTypeSnapshot',
    x.draft->>'topicSnapshot',
    x.draft->>'topicOpenAlexIdSnapshot',
    x.draft->>'abstractText',
    x.draft->>'doi',
    x.draft->>'pdfUrl',
    (x.draft->>'keywordsJson')::jsonb,
    x.draft->>'primaryFieldSnapshot',
    x.draft->>'subfieldSnapshot'
FROM jsonb_array_elements(p_drafts_json) AS x(draft)
ON CONFLICT (user_id, work_openalex_id)
    DO UPDATE SET
    title_snapshot = COALESCE(NULLIF(research_feed_item.title_snapshot, ''), EXCLUDED.title_snapshot),
           authors_snapshot = COALESCE(NULLIF(EXCLUDED.authors_snapshot, ''), research_feed_item.authors_snapshot),
           source_snapshot = COALESCE(NULLIF(research_feed_item.source_snapshot, ''), EXCLUDED.source_snapshot),
           publication_year = COALESCE(research_feed_item.publication_year, EXCLUDED.publication_year),
           publication_date = COALESCE(research_feed_item.publication_date, EXCLUDED.publication_date),
           citation_snapshot = COALESCE(research_feed_item.citation_snapshot, EXCLUDED.citation_snapshot),
           reason_json = CASE
           WHEN research_feed_item.reason_json IS NULL OR research_feed_item.reason_json = '{"reasons": []}'::jsonb
           THEN EXCLUDED.reason_json
           ELSE research_feed_item.reason_json
END,
        author_openalex_ids_snapshot = COALESCE(NULLIF(EXCLUDED.author_openalex_ids_snapshot, ''), research_feed_item.author_openalex_ids_snapshot),
        work_type_snapshot = COALESCE(NULLIF(research_feed_item.work_type_snapshot, ''), EXCLUDED.work_type_snapshot),
        topic_snapshot = COALESCE(NULLIF(research_feed_item.topic_snapshot, ''), EXCLUDED.topic_snapshot),
        topic_openalex_id_snapshot = COALESCE(NULLIF(research_feed_item.topic_openalex_id_snapshot, ''), EXCLUDED.topic_openalex_id_snapshot),
        abstract_text = COALESCE(NULLIF(research_feed_item.abstract_text, ''), EXCLUDED.abstract_text),
        doi = COALESCE(NULLIF(research_feed_item.doi, ''), EXCLUDED.doi),
        pdf_url = COALESCE(NULLIF(research_feed_item.pdf_url, ''), EXCLUDED.pdf_url),
        keywords_json = COALESCE(research_feed_item.keywords_json, EXCLUDED.keywords_json),
        primary_fields_snapshot = COALESCE(NULLIF(research_feed_item.primary_fields_snapshot, ''), EXCLUDED.primary_fields_snapshot),
        subfield_snapshot = COALESCE(NULLIF(research_feed_item.subfield_snapshot, ''), EXCLUDED.subfield_snapshot);

GET DIAGNOSTICS v_affected_rows = ROW_COUNT;
RETURN v_affected_rows;
END;
$$;