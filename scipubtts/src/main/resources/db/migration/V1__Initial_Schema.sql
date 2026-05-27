-- ==============================================================================
-- 0. CLEANUP (Optional: Remove existing tables to ensure a clean slate)
-- ==============================================================================
DROP TABLE IF EXISTS working_keywords CASCADE;
DROP TABLE IF EXISTS keywords CASCADE;
DROP TABLE IF EXISTS topics CASCADE;
DROP TABLE IF EXISTS subfields CASCADE;
DROP TABLE IF EXISTS fields CASCADE;

-- ==============================================================================
-- 1. FIELDS
-- ==============================================================================
CREATE TABLE fields (
    openalex_id TEXT PRIMARY KEY, -- Notes: currently only "17"
    name TEXT NOT NULL            -- Notes: "Computer Science"
);

-- ==============================================================================
-- 2. SUBFIELDS
-- ==============================================================================
CREATE TABLE subfields (
    openalex_id TEXT PRIMARY KEY, -- Notes: OpenAlex subfield ID
    field_id TEXT NOT NULL,       -- Notes: → fields.openalex_id
    name TEXT NOT NULL,           -- Notes: subfield display name
    
    -- Explicitly named FK constraint
    CONSTRAINT fk_subfields_field_id 
        FOREIGN KEY (field_id) 
        REFERENCES fields(openalex_id) 
        ON DELETE CASCADE
);

-- Index for FK performance
CREATE INDEX idx_subfields_field_id ON subfields(field_id);

-- ==============================================================================
-- 3. TOPICS
-- ==============================================================================
CREATE TABLE topics (
    openalex_id TEXT PRIMARY KEY,              -- Notes: topic ID
    subfield_id TEXT NOT NULL,                 -- Notes: → subfields.openalex_id
    name TEXT NOT NULL,                        -- Notes: topic display name
    description TEXT,                          -- Notes: topic summary (NULL)
    works_count BIGINT NOT NULL DEFAULT 0,     -- Notes: total works
    cited_by_count BIGINT NOT NULL DEFAULT 0,  -- Notes: total citations
    updated_date TIMESTAMPTZ,                  -- Notes: OpenAlex updated_date (NULL)
    
    -- Explicitly named FK constraint
    CONSTRAINT fk_topics_subfield_id 
        FOREIGN KEY (subfield_id) 
        REFERENCES subfields(openalex_id) 
        ON DELETE CASCADE
);

-- Index for FK performance
CREATE INDEX idx_topics_subfield_id ON topics(subfield_id);
