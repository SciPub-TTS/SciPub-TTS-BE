UPDATE users
SET avatar_url = REPLACE(
    avatar_url,
    'https://api.dicebear.com/9.x/',
    'https://api.dicebear.com/10.x/'
)
WHERE avatar_url LIKE 'https://api.dicebear.com/9.x/%';
