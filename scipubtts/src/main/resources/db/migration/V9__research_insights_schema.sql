-- =============================================================================
-- V8 — Research socials module
-- Tables: social_post, social_post_reference, social_post_like
-- =============================================================================

-- 1. Bài viết social
CREATE TABLE social_post (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid(),
    author_id       UUID                     NOT NULL,
    title           TEXT                     NOT NULL,
    body            TEXT                     NOT NULL,
    topic_tag       VARCHAR(100),
    like_count      INTEGER                  NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ              NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_social_post PRIMARY KEY (id),
    CONSTRAINT fk_social_post_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_social_post_like_count CHECK (like_count >= 0)
);

-- 2. Tài liệu tham chiếu đính kèm bài viết (snapshot từ OpenAlex)
--    Tối đa 3 references / post — enforce ở tầng Service
CREATE TABLE social_post_reference (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid(),
    post_id         UUID                     NOT NULL,
    openalex_id     VARCHAR(50)              NOT NULL,   -- chuẩn hóa: W123456789
    title_snapshot  TEXT,
    authors_snapshot TEXT,
    source_snapshot  VARCHAR(255),
    year_snapshot   SMALLINT,
    doi_snapshot    VARCHAR(255),

    CONSTRAINT pk_social_post_reference PRIMARY KEY (id),
    CONSTRAINT fk_social_post_reference_post
        FOREIGN KEY (post_id) REFERENCES social_post(id) ON DELETE CASCADE,
    CONSTRAINT uq_social_post_reference_openalex UNIQUE (post_id, openalex_id)
);

-- 3. Lượt tim (1 user chỉ like 1 lần / bài)
CREATE TABLE social_post_like (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid(),
    post_id         UUID                     NOT NULL,
    user_id         UUID                     NOT NULL,
    created_at      TIMESTAMPTZ              NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_social_post_like PRIMARY KEY (id),
    CONSTRAINT fk_social_post_like_post
        FOREIGN KEY (post_id) REFERENCES social_post(id) ON DELETE CASCADE,
    CONSTRAINT fk_social_post_like_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_social_post_like UNIQUE (post_id, user_id)
);

-- =============================================================================
-- Indexes
-- =============================================================================

-- Feed queries (newest / top) — chỉ lấy bài chưa xóa
CREATE INDEX idx_social_post_feed
    ON social_post (created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_social_post_top
    ON social_post (like_count DESC, created_at DESC)
    WHERE deleted_at IS NULL;

-- Lookup by author
CREATE INDEX idx_social_post_author
    ON social_post (author_id)
    WHERE deleted_at IS NULL;

-- Lookup references của 1 post
CREATE INDEX idx_social_post_reference_post
    ON social_post_reference (post_id);

-- Check like nhanh (dùng trong hybrid-view)
CREATE INDEX idx_social_post_like_user_post
    ON social_post_like (user_id, post_id);
