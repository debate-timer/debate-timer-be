package com.debatetimer.domain.parliamentary;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParliamentaryTableTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @ValueSource(strings = {"a bc가다9", "가0나 다ab"})
        void 테이블_이름은_영문과_한글_숫자_띄어쓰기만_가능하다(String name) {
            Member member = new Member("member", "default@gmail.com");
            assertThatCode(() -> new ParliamentaryTable(member, name, "agenda", 10))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, ParliamentaryTable.NAME_MAX_LENGTH + 1})
        void 테이블_이름은_정해진_길이_이내여야_한다(int length) {
            Member member = new Member("member", "default@gmail.com");
            assertThatThrownBy(() -> new ParliamentaryTable(member, "f".repeat(length), "agenda", 10))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "\t", "\n"})
        void 테이블_이름은_적어도_한_자_있어야_한다(String name) {
            Member member = new Member("member", "default@gmail.com");
            assertThatThrownBy(() -> new ParliamentaryTable(member, name, "agenda", 10))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_LENGTH.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc@", "가나다*", "abc\tde"})
        void 허용된_글자_이외의_문자는_불가능하다(String name) {
            Member member = new Member("member", "default@gmail.com");
            assertThatThrownBy(() -> new ParliamentaryTable(member, name, "agenda", 10))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_NAME_FORM.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -60})
        void 테이블_시간은_양수만_가능하다(int duration) {
            Member member = new Member("member", "default@gmail.com");
            assertThatThrownBy(() -> new ParliamentaryTable(member, "nickname", "agenda", duration))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_TABLE_TIME.getMessage());
        }
    }
}
