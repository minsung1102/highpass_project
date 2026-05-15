-- Ensure existing chat rooms appear in the owner's room list.
-- A chat room is listed only when chat_participant has a row for the user.

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_room')
    AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'owner_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'chat_room_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'user_id'),
    'INSERT INTO chat_participant (chat_room_id, user_id, is_owner, is_online, last_read_at, joined_at, status)
     SELECT cr.id, cr.owner_id, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ''JOINED''
     FROM chat_room cr
     WHERE cr.owner_id IS NOT NULL
       AND NOT EXISTS (
           SELECT 1 FROM chat_participant cp
           WHERE cp.chat_room_id = cr.id AND cp.user_id = cr.owner_id
       )',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;

SET @statement = IF(
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_room')
    AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'chat_participant')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_room' AND column_name = 'owner_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'chatRoom_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'chat_participant' AND column_name = 'user_id'),
    'INSERT INTO chat_participant (chatRoom_id, user_id, is_owner, is_online, last_read_at, joined_at, status)
     SELECT cr.id, cr.owner_id, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ''JOINED''
     FROM chat_room cr
     WHERE cr.owner_id IS NOT NULL
       AND NOT EXISTS (
           SELECT 1 FROM chat_participant cp
           WHERE cp.chatRoom_id = cr.id AND cp.user_id = cr.owner_id
       )',
    'SELECT 1'
);
PREPARE prepared_statement FROM @statement;
EXECUTE prepared_statement;
DEALLOCATE PREPARE prepared_statement;
