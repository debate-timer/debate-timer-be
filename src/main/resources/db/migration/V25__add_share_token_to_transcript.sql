ALTER TABLE transcript
    ADD COLUMN share_token VARCHAR(36) NULL;

ALTER TABLE transcript
    ADD CONSTRAINT uk_transcript_share_token UNIQUE (share_token);
