package com.debatetimer.repository.organization;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrganizationTemplateJdbcRepository {

    // 단체의 언어를 읽는 것과 템플릿 저장을 한 문장으로 처리해, 단체 언어 수정과 겹쳐도 템플릿이 이전 언어로 남지 않게 한다
    private static final String INSERT_WITH_ORGANIZATION_LANGUAGE = """
            INSERT INTO organization_template (organization_id, name, data, language, created_at, modified_at)
            SELECT o.id, ?, ?, o.language, ?, ?
            FROM organization o
            WHERE o.id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public Optional<Long> insertWithOrganizationLanguage(long organizationId, String name, String data) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int inserted = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    INSERT_WITH_ORGANIZATION_LANGUAGE, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, data);
            statement.setTimestamp(3, now);
            statement.setTimestamp(4, now);
            statement.setLong(5, organizationId);
            return statement;
        }, keyHolder);
        if (inserted == 0) {
            return Optional.empty();
        }
        return Optional.of(keyHolder.getKey().longValue());
    }
}
