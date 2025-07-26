package com.debatetimer.domain.poll;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.NullAndEmptyAndBlankSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;

class ParticipantNameTest {

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 투표참여자_이름은_널이거나_빈_값_일_수_없다(String name) {
            assertThatThrownBy(() -> new ParticipantName(name))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_POLL_PARTICIPANT_NAME.getMessage());
        }
    }
}
