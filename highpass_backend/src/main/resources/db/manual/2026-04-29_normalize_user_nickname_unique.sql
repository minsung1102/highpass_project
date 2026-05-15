UPDATE users
SET nickname = REGEXP_REPLACE(TRIM(nickname), '\\s+', '')
WHERE nickname IS NOT NULL;

ALTER TABLE users
    MODIFY COLUMN nickname VARCHAR(50) NOT NULL,
    ADD CONSTRAINT uk_user_nickname UNIQUE (nickname);
