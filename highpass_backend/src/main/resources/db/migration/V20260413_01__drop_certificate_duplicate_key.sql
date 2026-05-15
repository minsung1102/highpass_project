-- Migrated from db/manual/2026-04-13_drop_certificate_duplicate_key.sql.
-- Conditional statements keep existing and empty schemas safe during rollout.

SET @statement = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'national_certificate'
          AND index_name = 'uk_certificate_duplicate_key'
    ),
    'ALTER TABLE national_certificate DROP INDEX uk_certificate_duplicate_key',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'national_certificate'
          AND column_name = 'duplicate_key'
    ),
    'ALTER TABLE national_certificate DROP COLUMN duplicate_key',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
