CREATE TABLE IF NOT EXISTS bookmark_collection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_bookmark_collection_user_name UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS collection_bookmark (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL REFERENCES bookmark_collection(id) ON DELETE CASCADE,
    bookmark_id UUID NOT NULL REFERENCES user_bookmark(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_collection_bookmark UNIQUE (collection_id, bookmark_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmark_collection_user
    ON bookmark_collection(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_collection_bookmark_collection
    ON collection_bookmark(collection_id);

CREATE INDEX IF NOT EXISTS idx_collection_bookmark_bookmark
    ON collection_bookmark(bookmark_id);
