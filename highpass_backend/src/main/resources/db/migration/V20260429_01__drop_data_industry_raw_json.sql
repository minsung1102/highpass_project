-- Migrated from db/manual/2026-04-29_drop_data_industry_raw_json.sql.

SET @statement = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'data_industry_certificate_schedule'
          AND column_name = 'raw_json'
    ),
    'ALTER TABLE data_industry_certificate_schedule DROP COLUMN raw_json',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
