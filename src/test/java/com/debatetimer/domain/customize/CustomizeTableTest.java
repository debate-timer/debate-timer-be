package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
}
