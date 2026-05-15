-- Migrated from db/manual/2026-04-24_create_reports_table.sql.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users'),
    'CREATE TABLE IF NOT EXISTS reports (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        reporter_user_id BIGINT NOT NULL,
        target_type VARCHAR(20) NOT NULL,
        target_id VARCHAR(64) NOT NULL,
        target_label VARCHAR(120) NOT NULL,
        reason_code VARCHAR(40) NOT NULL,
        reason VARCHAR(1000) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT ''PENDING'',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_reports_reporter_user FOREIGN KEY (reporter_user_id) REFERENCES users (id)
    )',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
