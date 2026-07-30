-- =============================================================================
-- V1__create_guardian_article_tables.sql
-- Lưu bài viết khoa học/công nghệ từ The Guardian API
-- =============================================================================

CREATE TABLE guardian_article (
                                  id              BIGSERIAL           NOT NULL,
                                  external_id     VARCHAR(255)        NOT NULL,           -- "technology/2026/jul/29/..."
                                  title           VARCHAR(500)        NOT NULL,           -- webTitle
                                  summary         TEXT,                                   -- fields.trailText
                                  author          VARCHAR(255),                           -- fields.byline
                                  thumbnail_url   VARCHAR(1000),                          -- fields.thumbnail
                                  source_url      VARCHAR(1000)       NOT NULL,           -- webUrl
                                  published_at    TIMESTAMP WITH TIME ZONE NOT NULL,      -- webPublicationDate
                                  category        VARCHAR(100),                           -- sectionName
                                  created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

                                  CONSTRAINT pk_guardian_article         PRIMARY KEY (id),
                                  CONSTRAINT uq_guardian_article_ext_id  UNIQUE (external_id)
);

-- Tag của bài viết (1 bài có nhiều tag)
CREATE TABLE guardian_article_tag (
                                      id          BIGSERIAL       NOT NULL,
                                      article_id  BIGINT          NOT NULL,
                                      tag_name    VARCHAR(255)    NOT NULL,

                                      CONSTRAINT pk_guardian_article_tag      PRIMARY KEY (id),
                                      CONSTRAINT fk_guardian_tag_article      FOREIGN KEY (article_id)
                                          REFERENCES guardian_article(id) ON DELETE CASCADE
);

CREATE INDEX idx_guardian_article_published
    ON guardian_article (published_at DESC);

CREATE INDEX idx_guardian_article_category
    ON guardian_article (category);

CREATE INDEX idx_guardian_article_tag_article
    ON guardian_article_tag (article_id);

CREATE INDEX idx_guardian_article_tag_name
    ON guardian_article_tag (tag_name);