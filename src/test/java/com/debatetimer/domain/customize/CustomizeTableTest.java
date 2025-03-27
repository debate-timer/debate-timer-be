package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CustomizeTableTest {

    @Nested
    class GetType {

        @Test
        void 사용자_지정_테이블_타입을_반환한다() {
            CustomizeTable customizeTable = new CustomizeTable();

            assertThat(customizeTable.getType()).isEqualTo(TableType.CUSTOMIZE);
        }
    }

    @Nested
    class ValidateTeamName {

        @ValueSource(ints = {0, CustomizeTable.TEAM_NAME_MAX_LENGTH + 1})
        @ParameterizedTest
        void 찬성_팀_이름은_정해진_길이_이내여야_한다(int length) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(
                    () -> new CustomizeTable(member, "name", "agenda", true, true, "f".repeat(length), "cons"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_LENGTH.getMessage());
        }

        @ValueSource(ints = {0, CustomizeTable.TEAM_NAME_MAX_LENGTH + 1})
        @ParameterizedTest
        void 반대_팀_이름은_정해진_길이_이내여야_한다(int length) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(
                    () -> new CustomizeTable(member, "name", "agenda", true, true, "pros", "f".repeat(length)))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_LENGTH.getMessage());
        }
    }
}
