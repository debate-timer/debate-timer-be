package com.debatetimer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DebateTableTest {

    @Nested
    class Validate {

        @ValueSource(strings = {"a bc가다9", "가0나 다ab"})
        @ParameterizedTest
        void 테이블_이름은_영문과_한글_숫자_띄어쓰기만_가능하다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatCode(() -> new DebateTableTestObject(member, name, "agenda", 10, true, true))
                    .doesNotThrowAnyException();
        }

        @ValueSource(ints = {0, DebateTable.NAME_MAX_LENGTH + 1})
        @ParameterizedTest
        void 테이블_이름은_정해진_길이_이내여야_한다(int length) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, "f".repeat(length), "agenda", 10, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ValueSource(strings = {"", "\t", "\n"})
        @ParameterizedTest
        void 테이블_이름은_적어도_한_자_있어야_한다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, name, "agenda", 10, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ValueSource(strings = {"abc@", "가나다*", "abc\tde"})
        @ParameterizedTest
        void 허용된_글자_이외의_문자는_불가능하다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, name, "agenda", 10, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_FORM.getMessage());
        }

        @ValueSource(ints = {0, -1, -60})
        @ParameterizedTest
        void 테이블_시간은_양수만_가능하다(int duration) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, "nickname", "agenda", duration, true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_TIME.getMessage());
        }
    }

    @Nested
    class Update {

        @Test
        void 테이블_정보를_업데이트_할_수_있다() {
            Member member = new Member("default@gmail.com");
            DebateTableTestObject table = new DebateTableTestObject(member, "tableName", "agenda", 10, true, true);
            DebateTableTestObject renewTable = new DebateTableTestObject(member, "newName", "newAgenda", 100, false,
                    false);

            table.updateTable(renewTable);

            assertAll(
                    () -> assertThat(table.getName()).isEqualTo("newName"),
                    () -> assertThat(table.getAgenda()).isEqualTo("newAgenda"),
                    () -> assertThat(table.getDuration()).isEqualTo(100),
                    () -> assertThat(table.isWarningBell()).isEqualTo(false),
                    () -> assertThat(table.isFinishBell()).isEqualTo(false)
            );
        }
    }

    private static class DebateTableTestObject extends DebateTable {

        public DebateTableTestObject(Member member,
                                     String name,
                                     String agenda,
                                     int duration,
                                     boolean warningBell,
                                     boolean finishBell) {
            super(member, name, agenda, duration, warningBell, finishBell);
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public TableType getType() {
            return null;
        }

        public void updateTable(DebateTableTestObject renewTable) {
            super.updateTable(renewTable);
        }
    }
}
