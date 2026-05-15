-- Migrated from db/manual/2026-04-29_normalize_user_nickname_unique.sql.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'nickname'),
    'UPDATE users SET nickname = REGEXP_REPLACE(TRIM(nickname), ''\\\\s+'', '''') WHERE nickname IS NOT NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'nickname'),
    'ALTER TABLE users MODIFY COLUMN nickname VARCHAR(50) NOT NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'nickname'
          AND non_unique = 0
    ),
    'ALTER TABLE users ADD CONSTRAINT uk_user_nickname UNIQUE (nickname)',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
