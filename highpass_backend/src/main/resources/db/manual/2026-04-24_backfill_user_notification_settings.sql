ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_comment_noti_on BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS is_like_noti_on BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE users
SET is_comment_noti_on = TRUE
WHERE is_comment_noti_on IS NULL OR is_comment_noti_on = FALSE;

UPDATE users
SET is_like_noti_on = TRUE
WHERE is_like_noti_on IS NULL OR is_like_noti_on = FALSE;
