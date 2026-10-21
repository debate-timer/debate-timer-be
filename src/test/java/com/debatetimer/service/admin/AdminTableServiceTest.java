package com.debatetimer.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.BellType;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.admin.AdminTableDetailResponse;
import com.debatetimer.dto.admin.AdminTablePageResponse;
import com.debatetimer.dto.admin.AdminTableResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.entity.customize.CustomizeTimeBoxEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class AdminTableServiceTest extends BaseServiceTest {

    @Autowired
    private AdminTableService adminTableService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CustomizeTableEntity saveTable(Member member, LocalDateTime createdAt) {
        CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
        jdbcTemplate.update("UPDATE customize_table SET created_at = ? WHERE id = ?", createdAt, table.getId());
        return table;
    }

    @Nested
    class GetTables {

        @Test
        void 모든_회원의_테이블을_최신_생성순으로_페이지로_조회한다() {
            Member member1 = memberGenerator.generate("member1@email.com");
            Member member2 = memberGenerator.generate("member2@email.com");
            CustomizeTableEntity oldest = saveTable(member1, LocalDateTime.of(2026, 10, 1, 0, 0));
            CustomizeTableEntity latest = saveTable(member2, LocalDateTime.of(2026, 10, 3, 0, 0));
            CustomizeTableEntity middle = saveTable(member1, LocalDateTime.of(2026, 10, 2, 0, 0));

            AdminTablePageResponse firstPage = adminTableService.getTables(0, 2);
            AdminTablePageResponse secondPage = adminTableService.getTables(1, 2);

            assertAll(
                    () -> assertThat(firstPage.tables()).extracting(AdminTableResponse::id)
                            .containsExactly(latest.getId(), middle.getId()),
                    () -> assertThat(firstPage.tables().get(0).memberEmail()).isEqualTo("member2@email.com"),
                    () -> assertThat(firstPage.totalElements()).isEqualTo(3),
                    () -> assertThat(firstPage.totalPages()).isEqualTo(2),
                    () -> assertThat(secondPage.tables()).extracting(AdminTableResponse::id)
                            .containsExactly(oldest.getId())
            );
        }

        @ParameterizedTest
        @CsvSource({"-1, 20", "0, 0", "0, 101"})
        void 페이지_요청이_잘못되면_예외를_던진다(int page, int size) {
            assertThatThrownBy(() -> adminTableService.getTables(page, size))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_PAGE_REQUEST.getMessage());
        }
    }

    @Nested
    class GetTable {

        @Test
        void 소유자와_관계없이_테이블과_타임박스_종소리를_조회한다() {
            Member member = memberGenerator.generate("member@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            CustomizeTimeBoxEntity normal = customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxRepository.save(new CustomizeTimeBoxEntity(
                    table, 2, Stance.NEUTRAL, "자유토론", CustomizeBoxType.TIME_BASED, 120, 60, null));
            bellEntityGenerator.generate(normal, BellType.BEFORE_END, 30, 1);

            AdminTableDetailResponse response = adminTableService.getTable(table.getId());

            assertAll(
                    () -> assertThat(response.memberEmail()).isEqualTo("member@email.com"),
                    () -> assertThat(response.table()).hasSize(2),
                    () -> assertThat(response.table().get(0).bell()).hasSize(1),
                    () -> assertThat(response.table().get(1).boxType()).isEqualTo(CustomizeBoxType.TIME_BASED)
            );
        }

        @Test
        void 테이블이_없으면_예외를_던진다() {
            assertThatThrownBy(() -> adminTableService.getTable(999L))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.TABLE_NOT_FOUND.getMessage());
        }
    }
}
