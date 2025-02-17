package com.debatetimer.domain.timebased;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeBasedTableTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @ValueSource(strings = {"a bc가다9", "가0나 다ab"})
        void 테이블_이름은_영문과_한글_숫자_띄어쓰기만_가능하다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatCode(() -> new TimeBasedTable(member, name, "agenda", 200, true, true))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, ParliamentaryTable.NAME_MAX_LENGTH + 1})
        void 테이블_이름은_정해진_길이_이내여야_한다(int length) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new TimeBasedTable(member, "f".repeat(length), "agenda", 10, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "\t", "\n"})
        void 테이블_이름은_적어도_한_자_있어야_한다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new TimeBasedTable(member, name, "agenda", 10, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc@", "가나다*", "abc\tde"})
        void 허용된_글자_이외의_문자는_불가능하다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new TimeBasedTable(member, name, "agenda", 10, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_FORM.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -60})
        void 테이블_시간은_양수만_가능하다(int duration) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new TimeBasedTable(member, "nickname", "agenda", duration, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_TIME.getMessage());
        }

        @Nested
        class Update {

            @Test
            void 테이블_정보를_업데이트_할_수_있다() {
                Member member = new Member("default@gmail.com");
                TimeBasedTable table = new TimeBasedTable(member, "tableName", "agenda", 10, true, true);
                TimeBasedTable renewTable = new TimeBasedTable(member, "newName", "newAgenda", 100, false, false);

                table.update(renewTable);

                assertAll(
                        () -> assertThat(table.getName()).isEqualTo("newName"),
                        () -> assertThat(table.getAgenda()).isEqualTo("newAgenda"),
                        () -> assertThat(table.getDuration()).isEqualTo(100),
                        () -> assertThat(table.isWarningBell()).isEqualTo(false),
                        () -> assertThat(table.isFinishBell()).isEqualTo(false)
                );
            }
        }
    }
}
