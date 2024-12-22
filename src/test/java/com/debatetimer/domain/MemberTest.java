package com.debatetimer.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @ValueSource(ints = {0, Member.NICKNAME_MAX_LENGTH + 1})
        void 닉네임은_1자_이상_10자_이하여야_한다(int length) {
            assertThatThrownBy(() -> new Member("f".repeat(length)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("닉네임은 1자 이상 %d자 이하여야 합니다", Member.NICKNAME_MAX_LENGTH));
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc12", "가나다12"})
        void 닉네임은_영문과_한글만_가능하다(String nickname) {
            assertThatThrownBy(() -> new Member(nickname))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("닉네임은 영문/한글만 가능합니다");
        }
    }
}
