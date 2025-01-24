package com.debatetimer.domain.member;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @ValueSource(strings = {"a bc가다", "가나 다ab"})
        void 닉네임은_영문과_한글_띄어쓰기만_가능하다(String nickname) {
            assertThatCode(() -> new Member(nickname))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, Member.NICKNAME_MAX_LENGTH + 1})
        void 닉네임은_정해진_길이_이내여야_한다(int length) {
            assertThatThrownBy(() -> new Member("f".repeat(length)))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_MEMBER_NICKNAME_LENGTH.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc12", "가나다12"})
        void 닉네임은_영문과_한글만_가능하다(String nickname) {
            assertThatThrownBy(() -> new Member(nickname))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_MEMBER_NICKNAME_FORM.getMessage());
        }
    }
}
