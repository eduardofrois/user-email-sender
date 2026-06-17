ALTER TABLE tb_email
    ADD COLUMN origin_event_type VARCHAR(100),
    ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN last_attempt_at TIMESTAMP,
    ADD COLUMN error_message TEXT;

UPDATE tb_email
SET
    attempts = COALESCE(attempts, 1),
    created_at = COALESCE(created_at, send_date_email),
    last_attempt_at = COALESCE(last_attempt_at, send_date_email),
    origin_event_type = COALESCE(origin_event_type, 'LEGACY_RECORD');
