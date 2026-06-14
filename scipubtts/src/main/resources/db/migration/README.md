Flyway migration rules for this project

1. Never edit a migration file that has already run in any shared environment.
2. For every new schema change, create a brand-new migration file instead of modifying an old one.
3. Use timestamp-based versions to avoid branch collisions when multiple people add DB changes in parallel.

Recommended filename format:

VYYYYMMDDHHMMSS__short_description.sql

Examples:

V20260613161000__add_saved_search_indexes.sql
V20260613164500__create_user_preferences_table.sql

Why this project enables out-of-order migrations:

- If branch A ships a higher version first and branch B is merged later with a lower timestamp/version,
  Flyway can still apply the missing migration safely instead of failing validation on startup.

What to do when Flyway still complains:

- Duplicate version number:
  Rename the new migration file before merge.
- Checksum mismatch:
  Do not edit the old migration again. Create a new fix migration instead.
- History table already contains the right migration but local file was changed accidentally:
  Restore the file content from git history. Only use Flyway repair when you are certain the database
  and the intended migration history are already correct.
