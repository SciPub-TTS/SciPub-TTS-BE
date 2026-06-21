ALTER TABLE users
ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(1024);

UPDATE users
SET avatar_url = 'https://api.dicebear.com/9.x/adventurer/svg?seed=' || id::text
WHERE avatar_url IS NULL;
