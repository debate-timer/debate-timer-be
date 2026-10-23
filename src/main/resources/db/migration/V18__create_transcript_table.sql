CREATE TABLE transcript
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id      BIGINT    NOT NULL,
    member_id     BIGINT    NOT NULL,
    status        ENUM ('PROCESSING','DONE','FAILED') NOT NULL,
    lines_json    LONGTEXT  NOT NULL,
    report_json   LONGTEXT NULL,
    error_message VARCHAR(255) NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_transcript_member_id ON transcript (member_id);
