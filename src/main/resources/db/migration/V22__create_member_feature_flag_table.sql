CREATE TABLE member_feature_flag
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT      NOT NULL,
    feature_flag VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_feature_flag_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT uk_member_feature_flag UNIQUE (member_id, feature_flag)
);
