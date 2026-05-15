-- Allow newly added notification enum values such as CHAT.
-- Some existing databases have notification.type as a MySQL ENUM or ordinal-sized
-- column created before CHAT was introduced, which truncates the value on insert.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'notification')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'notification' AND column_name = 'type'),
    'ALTER TABLE notification MODIFY type VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'notification' AND column_name = 'type'),
    'UPDATE notification SET type = CASE WHEN type = ''0'' THEN ''COMMENT'' WHEN type = ''1'' THEN ''LIKE'' WHEN type = ''2'' THEN ''CALENDAR'' WHEN type = ''3'' THEN ''CHAT'' WHEN type IN (''COMMENT'', ''LIKE'', ''CALENDAR'', ''CHAT'') THEN type ELSE ''COMMENT'' END',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'notification' AND column_name = 'type'),
    'ALTER TABLE notification MODIFY type VARCHAR(20) NOT NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
