CREATE TABLE sharing_log
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id       BIGINT    NOT NULL,
    member_id      BIGINT    NOT NULL,
    started_at     TIMESTAMP NOT NULL,
    ended_at       TIMESTAMP NULL,
    audience_count INT       NOT NULL DEFAULT 0,
    status         ENUM ('SHARING','FINISHED','ABANDONED') NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_sharing_log_status_started_at ON sharing_log (status, started_at);
