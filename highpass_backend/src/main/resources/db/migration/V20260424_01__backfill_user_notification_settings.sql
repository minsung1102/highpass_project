-- Migrated from db/manual/2026-04-24_backfill_user_notification_settings.sql.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'is_comment_noti_on'),
    'ALTER TABLE users ADD COLUMN is_comment_noti_on BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'is_like_noti_on'),
    'ALTER TABLE users ADD COLUMN is_like_noti_on BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'is_comment_noti_on'),
    'UPDATE users SET is_comment_noti_on = TRUE WHERE is_comment_noti_on IS NULL OR is_comment_noti_on = FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'is_like_noti_on'),
    'UPDATE users SET is_like_noti_on = TRUE WHERE is_like_noti_on IS NULL OR is_like_noti_on = FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
