CREATE TABLE poll
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id       BIGINT       NOT NULL,
    status VARCHAR(255) NOT NULL,
    pros_team_name VARCHAR(255) NOT NULL,
    cons_team_name VARCHAR(255) NOT NULL,
    agenda         TEXT,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL
);

CREATE TABLE vote
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    poll_id          BIGINT       NOT NULL,
    team VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    participant_code VARCHAR(255) NOT NULL,
    CONSTRAINT fk_vote_poll FOREIGN KEY (poll_id) REFERENCES poll (id) ON DELETE CASCADE
);
