ALTER TABLE transcript
    ADD COLUMN table_name VARCHAR(255) NULL;

ALTER TABLE transcript
    ADD COLUMN agenda VARCHAR(255) NULL;

ALTER TABLE transcript
    ADD COLUMN pros_team_name VARCHAR(255) NULL;

ALTER TABLE transcript
    ADD COLUMN cons_team_name VARCHAR(255) NULL;

-- 기존 속기록은 지금 시간표 값으로 채운다. 이미 지워진 시간표의 기록은 비워 둔다.
UPDATE transcript
SET table_name     = (SELECT c.name FROM customize_table c WHERE c.id = transcript.table_id),
    agenda         = (SELECT c.agenda FROM customize_table c WHERE c.id = transcript.table_id),
    pros_team_name = (SELECT c.pros_team_name FROM customize_table c WHERE c.id = transcript.table_id),
    cons_team_name = (SELECT c.cons_team_name FROM customize_table c WHERE c.id = transcript.table_id);
