-- Backfill chat columns that were added after chat data already existed.
-- Hibernate update can add these columns without usable defaults for old rows,
-- which then causes room list/message mapping to fail at runtime.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_room')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'type'),
    'ALTER TABLE chat_room ADD COLUMN type VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_room')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'owner_id'),
    'ALTER TABLE chat_room ADD COLUMN owner_id BIGINT NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_room')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'is_approval_required'),
    'ALTER TABLE chat_room ADD COLUMN is_approval_required BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'last_read_at'),
    'ALTER TABLE chat_participant ADD COLUMN last_read_at DATETIME NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'joined_at'),
    'ALTER TABLE chat_participant ADD COLUMN joined_at DATETIME NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'is_owner'),
    'ALTER TABLE chat_participant ADD COLUMN is_owner BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'is_online'),
    'ALTER TABLE chat_participant ADD COLUMN is_online BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'status'),
    'ALTER TABLE chat_participant ADD COLUMN status VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_message')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_message' AND column_name = 'type'),
    'ALTER TABLE chat_message ADD COLUMN type VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_message')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_message' AND column_name = 'deleted'),
    'ALTER TABLE chat_message ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'chat_room'
        AND column_name = 'type'
        AND data_type NOT IN ('char', 'varchar', 'text', 'tinytext', 'mediumtext', 'longtext')
    ),
    'ALTER TABLE chat_room MODIFY type VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'type'),
    'UPDATE chat_room SET type = CASE WHEN type = ''0'' THEN ''PERSONAL'' WHEN type = ''1'' THEN ''GROUP'' WHEN type IN (''PERSONAL'', ''GROUP'') THEN type ELSE ''PERSONAL'' END',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'study_board')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'study_board' AND column_name = 'chat_room_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'study_board' AND column_name = 'user_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'owner_id'),
    'UPDATE chat_room cr JOIN study_board sb ON sb.chat_room_id = cr.id SET cr.owner_id = sb.user_id, cr.type = ''GROUP'' WHERE cr.owner_id IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'is_approval_required'),
    'UPDATE chat_room SET is_approval_required = FALSE WHERE is_approval_required IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'is_approval_required'),
    'ALTER TABLE chat_room MODIFY is_approval_required BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'chat_participant'
        AND column_name = 'status'
        AND data_type NOT IN ('char', 'varchar', 'text', 'tinytext', 'mediumtext', 'longtext')
    ),
    'ALTER TABLE chat_participant MODIFY status VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'status'),
    'UPDATE chat_participant SET status = CASE WHEN status = ''0'' THEN ''PENDING'' WHEN status = ''1'' THEN ''JOINED'' WHEN status = ''2'' THEN ''REJECTED'' WHEN status IN (''PENDING'', ''JOINED'', ''REJECTED'') THEN status ELSE ''JOINED'' END',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'joined_at'),
    'UPDATE chat_participant SET joined_at = CURRENT_TIMESTAMP WHERE joined_at IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'last_read_at'),
    'UPDATE chat_participant SET last_read_at = joined_at WHERE last_read_at IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'is_owner'),
    'UPDATE chat_participant SET is_owner = FALSE WHERE is_owner IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'is_online'),
    'UPDATE chat_participant SET is_online = FALSE WHERE is_online IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'is_owner'),
    'ALTER TABLE chat_participant MODIFY is_owner BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'is_online'),
    'ALTER TABLE chat_participant MODIFY is_online BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_message' AND column_name = 'type'),
    'ALTER TABLE chat_message MODIFY type VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_message' AND column_name = 'type'),
    'UPDATE chat_message SET type = CASE WHEN type = ''0'' THEN ''TALK'' WHEN type = ''1'' THEN ''ENTER'' WHEN type = ''2'' THEN ''QUIT'' WHEN type = ''3'' THEN ''NOTICE'' WHEN type IN (''TALK'', ''ENTER'', ''QUIT'', ''NOTICE'') THEN type ELSE ''TALK'' END',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_message' AND column_name = 'deleted'),
    'UPDATE chat_message SET deleted = FALSE WHERE deleted IS NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'last_read_at'),
    'ALTER TABLE chat_participant MODIFY last_read_at DATETIME NOT NULL',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
