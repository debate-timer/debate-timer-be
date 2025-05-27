package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
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
    class ValidateTableName {

        @ValueSource(strings = {"a bc가다9", "가0나 다ab", "ㄱㄷㅇㄹ", "漢字", "にほんご", "vielfältig"})
        @ParameterizedTest
        void 테이블_이름은_이모지를_제외한_글자만_가능하다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatCode(() -> new CustomizeTable(member, name, "agenda", true, true, "pros", "cons"))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"a😀bc가다9", "🐥", "🥦"})
        @ParameterizedTest
        void 테이블_이름에_이모지를_넣을_수_없다(String name) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new CustomizeTable(member, name, "agenda", true, true, "pros", "cons"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_FORM.getMessage());
        }

        @Test
        void 테이블_이름은_정해진_길이_이내여야_한다() {
            Member member = new Member("default@gmail.com");
            String longTableName = "f".repeat(CustomizeTable.TABLE_NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new CustomizeTable(member, longTableName, "agenda", true, true, "pros", "cons"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }
    }

    @Nested
    class ValidateTeamName {

        @Test
        void 찬성_팀_이름은_정해진_길이_이내여야_한다() {
            Member member = new Member("default@gmail.com");
            String longProsTeamName = "f".repeat(CustomizeTable.TEAM_NAME_MAX_LENGTH + 1);

            assertThatThrownBy(
                    () -> new CustomizeTable(member, "name", "agenda", true, true, longProsTeamName, "cons"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_LENGTH.getMessage());
        }

        @Test
        void 반대_팀_이름은_정해진_길이_이내여야_한다() {
            Member member = new Member("default@gmail.com");
            String longConsTeamName = "f".repeat(CustomizeTable.TEAM_NAME_MAX_LENGTH + 1);

            assertThatThrownBy(
                    () -> new CustomizeTable(member, "name", "agenda", true, true, "pros", longConsTeamName))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_LENGTH.getMessage());
        }

        @ValueSource(strings = {"a bc가다9", "가0나 다ab", "ㄱㄷㅇㄹ", "漢字", "にほんご", "vielfäl"})
        @ParameterizedTest
        void 찬성_팀_이름은_이모지를_제외한_글자만_가능하다(String prosName) {
            Member member = new Member("default@gmail.com");
            assertThatCode(() -> new CustomizeTable(member, "name", "agenda", true, true, prosName, "cons"))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"a😀가다9", "🐥", "🥦"})
        @ParameterizedTest
        void 찬성_팀_이름에_이모지를_넣을_수_없다(String prosName) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new CustomizeTable(member, "name", "agenda", true, true, prosName, "cons"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_FORM.getMessage());
        }

        @ValueSource(strings = {"a bc가다9", "가0나 다ab", "ㄱㄷㅇㄹ", "漢字", "にほんご", "vielfäl"})
        @ParameterizedTest
        void 반대_팀_이름은_이모지를_제외한_글자만_가능하다(String consName) {
            Member member = new Member("default@gmail.com");
            assertThatCode(() -> new CustomizeTable(member, "name", "agenda", true, true, "pros", consName))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"a😀가다9", "🐥", "🥦"})
        @ParameterizedTest
        void 반대_팀_이름에_이모지를_넣을_수_없다(String consName) {
            Member member = new Member("default@gmail.com");
            assertThatThrownBy(() -> new CustomizeTable(member, "name", "agenda", true, true, "pros", consName))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_FORM.getMessage());
        }
    }

    @Nested
    class UpdateUsedAt {

        @Test
        void 테이블의_사용_시각을_업데이트한다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "tableName", "agenda", true, true, "찬성", "반대");
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
            CustomizeTable table = new CustomizeTable(member, "tableName", "agenda", true, true, "pros", "cons");
            DebateTable renewTable = new DebateTable(member, new TableName("newName"), "newAgenda",
                    new TeamName("newPros"), new TeamName("newCons"), false, false);

            table.updateTable(renewTable);

            assertAll(
                    () -> assertThat(table.getName()).isEqualTo("newName"),
                    () -> assertThat(table.getAgenda()).isEqualTo("newAgenda"),
                    () -> assertThat(table.getProsTeamName()).isEqualTo("newPros"),
                    () -> assertThat(table.getConsTeamName()).isEqualTo("newCons"),
                    () -> assertThat(table.isWarningBell()).isFalse(),
                    () -> assertThat(table.isFinishBell()).isFalse()
            );
        }

        @Test
        void 테이블_업데이트_할_때_사용_시간을_변경한다() throws InterruptedException {
            Member member = new Member("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "tableName", "agenda", true, true, "pros", "cons");
            DebateTable renewTable = new DebateTable(member, new TableName("newName"), "newAgenda",
                    new TeamName("newPros"), new TeamName("newcons"), false, false);
            LocalDateTime beforeUsedAt = table.getUsedAt();
            Thread.sleep(1);

            table.updateTable(renewTable);

            assertThat(table.getUsedAt()).isAfter(beforeUsedAt);
        }
    }
}
