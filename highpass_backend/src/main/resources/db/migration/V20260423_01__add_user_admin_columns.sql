-- Migrated from db/manual/2026-04-23_add_user_admin_columns.sql.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'status'),
    'ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''ACTIVE''',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'last_login_at'),
    'ALTER TABLE users ADD COLUMN last_login_at DATETIME NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'last_seen_at'),
    'ALTER TABLE users ADD COLUMN last_seen_at DATETIME NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'role'),
    'ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''USER''',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users'),
    'UPDATE users SET role = ''ADMIN'', status = ''ACTIVE'' WHERE email = ''admin@highpass.local''',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
