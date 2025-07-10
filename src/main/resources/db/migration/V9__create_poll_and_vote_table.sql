CREATE TABLE poll
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id       BIGINT       NOT NULL,
    status      ENUM ('PROGRESS','DONE') NOT NULL,
    pros_team_name VARCHAR(255) NOT NULL,
    cons_team_name VARCHAR(255) NOT NULL,
    agenda      VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    modified_at TIMESTAMP    NOT NULL
);

CREATE TABLE vote
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    poll_id          BIGINT       NOT NULL,
    team ENUM ('CONS','PROS') NOT NULL,
    name             VARCHAR(255) NOT NULL,
    participant_code VARCHAR(255) NOT NULL
);

alter table vote
    add constraint vote_to_poll
        foreign key (poll_id)
            references poll (id);
