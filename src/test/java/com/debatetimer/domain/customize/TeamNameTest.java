package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TeamNameTest {

    @Nested
    class Validate {

        @Test
        void 팀_이름은_정해진_길이_이내여야_한다() {
            String longTeamName = "f".repeat(TeamName.NAME_MAX_LENGTH + 1);

            assertThatThrownBy(
                    () -> new TeamName(longTeamName))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_LENGTH.getMessage());
        }

        @ValueSource(strings = {"a bc가다9", "가0나 다ab", "ㄱㄷㅇㄹ", "漢字", "にほんご", "vielfäl"})
        @ParameterizedTest
        void 팀_이름은_이모지를_제외한_글자만_가능하다(String validTeamName) {
            assertThatCode(() -> new TeamName(validTeamName))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"a😀가다9", "🐥", "🥦"})
        @ParameterizedTest
        void 팀_이름에_이모지를_넣을_수_없다(String invalidTeamName) {
            assertThatThrownBy(() -> new TeamName(invalidTeamName))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TEAM_NAME_FORM.getMessage());
        }
    }
}
