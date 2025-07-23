package com.debatetimer.domain.poll;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ParticipantNameTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", " "})
        void 투표참여자_이름은_널이거나_빈_값_일_수_없다(String name) {
            assertThatThrownBy(() -> new ParticipantName(name))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_POLL_PARTICIPANT_NAME.getMessage());
        }
    }
}
