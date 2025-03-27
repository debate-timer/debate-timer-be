package com.debatetimer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DebateTableTest {

    @Nested
    class Validate {

        @ValueSource(strings = {"a bc가다9", "가0나 다ab", "ㄱㄷㅇㄹ", "漢字", "にほんご", "vielfältig"})
        @ParameterizedTest
        void 테이블_이름은_이모지를_제외한_글자만_가능하다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatCode(() -> new DebateTableTestObject(member, name, "agenda", true, true))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"a😀bc가다9", "🐥", "🥦"})
        @ParameterizedTest
        void 테이블_이름에_이모지를_넣을_수_없다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, name, "agenda", true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_FORM.getMessage());
        }

        @ValueSource(ints = {0, DebateTable.NAME_MAX_LENGTH + 1})
        @ParameterizedTest
        void 테이블_이름은_정해진_길이_이내여야_한다(int length) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, "f".repeat(length), "agenda", true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ValueSource(strings = {"", "\t", "\n"})
        @ParameterizedTest
        void 테이블_이름은_적어도_한_자_있어야_한다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new DebateTableTestObject(member, name, "agenda", true, true))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 테이블의_사용_시각을_업데이트한다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            DebateTableTestObject table = new DebateTableTestObject(member, "tableName", "agenda", true, true);
            LocalDateTime beforeUsedAt = table.getUsedAt();
            Thread.sleep(1);

            table.updateUsedAt();

            assertThat(table.getUsedAt()).isAfter(beforeUsedAt);
        }
    }

    @Nested
    class Update {

        @Test
        void 테이블_정보를_업데이트_할_수_있다() {
            Member member = new Member("default@gmail.com");
            DebateTableTestObject table = new DebateTableTestObject(member, "tableName", "agenda", true, true);
            DebateTableTestObject renewTable = new DebateTableTestObject(member, "newName", "newAgenda", false,
                    false);

            table.updateTable(renewTable);

            assertAll(
                    () -> assertThat(table.getName()).isEqualTo("newName"),
                    () -> assertThat(table.getAgenda()).isEqualTo("newAgenda"),
                    () -> assertThat(table.isWarningBell()).isEqualTo(false),
                    () -> assertThat(table.isFinishBell()).isEqualTo(false)
            );
        }

        @Test
        void 테이블_업데이트_할_때_사용_시간을_변경한다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            DebateTableTestObject table = new DebateTableTestObject(member, "tableName", "agenda", true, true);
            DebateTableTestObject renewTable = new DebateTableTestObject(member, "newName", "newAgenda", false,
                    false);
            LocalDateTime beforeUsedAt = table.getUsedAt();
            Thread.sleep(1);

            table.updateTable(renewTable);

            assertThat(table.getUsedAt()).isAfter(beforeUsedAt);
        }
    }

    private static class DebateTableTestObject extends DebateTable {

        public DebateTableTestObject(Member member,
                                     String name,
                                     String agenda,
                                     boolean warningBell,
                                     boolean finishBell) {
            super(member, name, agenda, warningBell, finishBell);
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
