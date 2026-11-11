-- 다기기 녹음 V2. V2를 걷어낼 때는 두 테이블을 지우면 된다.
CREATE TABLE recording_session_v2
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_key    VARCHAR(36) NOT NULL,
    member_id      BIGINT      NOT NULL,
    table_id       BIGINT      NOT NULL,
    language       VARCHAR(16) NULL,
    join_code_hash VARCHAR(64) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    transcript_id  BIGINT NULL,
    error_message  VARCHAR(255) NULL,
    finished_at    TIMESTAMP NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_recording_session_v2_session_key UNIQUE (session_key)
);

CREATE INDEX idx_recording_session_v2_status ON recording_session_v2 (status);

CREATE TABLE recording_turn_v2
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id          BIGINT      NOT NULL,
    turn_key            VARCHAR(64) NOT NULL,
    time_box_index      INT         NOT NULL,
    team                VARCHAR(10) NULL,
    started_at_epoch_ms BIGINT      NOT NULL,
    status              VARCHAR(20) NOT NULL,
    candidate_count     INT         NOT NULL,
    selected_device_id  VARCHAR(64) NULL,
    s3_key              VARCHAR(512) NULL,
    utterances_json     LONGTEXT NULL,
    error_message       VARCHAR(255) NULL,
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recording_turn_v2_session FOREIGN KEY (session_id) REFERENCES recording_session_v2 (id),
    CONSTRAINT uk_recording_turn_v2_session_turn UNIQUE (session_id, turn_key)
);
