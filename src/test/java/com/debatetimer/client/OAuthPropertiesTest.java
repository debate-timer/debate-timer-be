package com.debatetimer.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTInitializationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OAuthPropertiesTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 클라이언트_아이디가_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new OAuthProperties(empty, "client_secret", "grant_type"))
                    .isInstanceOf(DTInitializationException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 클라이언트_비밀키가_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new OAuthProperties("client_id", empty, "grant_type"))
                    .isInstanceOf(DTInitializationException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 타입이_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new OAuthProperties("client_id", "client_secret", empty))
                    .isInstanceOf(DTInitializationException.class);
        }
    }
}
