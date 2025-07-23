package com.debatetimer.domain.poll;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ParticipateCodeTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", " "})
        void 투표_참여_코드는_널이거나_빈_값_일_수_없다(String participatecode) {
            assertThatThrownBy(() -> new ParticipateCode(participatecode))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_POLL_PARTICIPANT_CODE.getMessage());
        }
    }
}
