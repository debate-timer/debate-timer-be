ALTER TABLE vote
    CHANGE participant_code participate_code VARCHAR (255) NOT NULL;

ALTER TABLE vote
    ADD CONSTRAINT uq_vote_poll_participate UNIQUE (poll_id, participate_code);
