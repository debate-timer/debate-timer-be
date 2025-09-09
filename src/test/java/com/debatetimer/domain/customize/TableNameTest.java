package com.debatetimer.domain.customize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TableNameTest {

    @Nested
    class Validate {

        @ValueSource(strings = {"a bc가다9", "가0나 다ab", "ㄱㄷㅇㄹ", "漢字", "にほんご", "vielfältig"})
        @ParameterizedTest
        void 테이블_이름은_이모지를_제외한_글자만_가능하다(String validName) {
            assertThatCode(() -> new TableName(validName))
                    .doesNotThrowAnyException();
        }

        @ValueSource(strings = {"a😀bc가다9", "🐥", "🥦"})
        @ParameterizedTest
        void 테이블_이름에_이모지를_넣을_수_없다(String invalidName) {
            assertThatThrownBy(() -> new TableName(invalidName))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_FORM.getMessage());
        }

        @Test
        void 테이블_이름은_정해진_길이_이내여야_한다() {
            String longTableName = "f".repeat(TableName.NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new TableName(longTableName))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }
    }
}
