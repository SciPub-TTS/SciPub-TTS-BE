-- Reset topics id sequence after V2 table rename (taxonomy_topics kept the old sequence).
SELECT setval(
        pg_get_serial_sequence('topics', 'id'),
        COALESCE((SELECT MAX(id) FROM topics), 0) + 1,
        false
);
