package com.debatetimer.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.client.notifier.DiscordProperties;
import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DiscordPropertiesTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 디스코드봇_토큰이_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new DiscordProperties(empty, "channelId"))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.DISCORD_PROPERTIES_EMPTY.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\n", "\t "})
        void 디스코드_채널_아이디가_비어있을_경우_예외를_발생시킨다(String empty) {
            assertThatThrownBy(() -> new DiscordProperties("token", empty))
                    .isInstanceOf(DTInitializationException.class)
                    .hasMessage(InitializationErrorCode.DISCORD_PROPERTIES_EMPTY.getMessage());
        }
    }
}
