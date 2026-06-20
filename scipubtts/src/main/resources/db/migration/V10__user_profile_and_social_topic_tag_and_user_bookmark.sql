-- =============================================================================
-- V10 — Schema for User Profile + Social Hub + User Bookmark
-- =============================================================================

-- 1. BẢNG USER PROFILE (Mới hoàn toàn)
CREATE TABLE IF NOT EXISTS user_profile (
    user_id           UUID                     NOT NULL,
    institution       VARCHAR(255),
    department        VARCHAR(255),
    country           VARCHAR(100)             NOT NULL DEFAULT 'Vietnam',
    created_at        TIMESTAMPTZ              NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ              NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_profile PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profile_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE social_post 
    ADD COLUMN IF NOT EXISTS topic_tag VARCHAR(100);

ALTER TABLE social_post_reference 
    DROP COLUMN IF EXISTS source_snapshot;

ALTER TABLE social_post_reference 
    DROP COLUMN IF EXISTS doi_snapshot;

ALTER TABLE social_post_reference 
    ADD COLUMN IF NOT EXISTS title_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS authors_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS citation_snapshot INT,
    ADD COLUMN IF NOT EXISTS year_snapshot SMALLINT;

-- 4. DỌN DẸP BẢNG USER_BOOKMARK (Nếu bạn muốn đồng bộ theo thiết kế mới)
-- Bỏ các cột share và public không dùng tới
ALTER TABLE user_bookmark 
    DROP COLUMN IF EXISTS share_token;

ALTER TABLE user_bookmark 
    DROP COLUMN IF EXISTS is_public;

-- 5. BỎ BẢNG CACHE (Nếu trước đó đã tồn tại)
DROP TABLE IF EXISTS openalex_entity_cache;

-- 6. INDEX TỐI ƯU (Nếu cần thiết cho hiệu năng)
-- Chỉ giữ index trên FK/PK quan trọng để đảm bảo DB nhẹ nhất
CREATE INDEX IF NOT EXISTS idx_social_post_author_id ON social_post(author_id);
CREATE INDEX IF NOT EXISTS idx_social_post_like_post_id ON social_post_like(post_id);