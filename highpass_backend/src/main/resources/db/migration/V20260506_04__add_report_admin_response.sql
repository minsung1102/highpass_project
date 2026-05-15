ALTER TABLE reports
    ADD COLUMN admin_response VARCHAR(2000) NULL,
    ADD COLUMN responded_at  DATETIME     NULL;
