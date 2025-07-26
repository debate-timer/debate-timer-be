package com.debatetimer.entity.customize;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.customize.BellType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BellEntityTest {

    @Nested
    class Validate {

        @Test
        void 벨_시간은_0이상이어야_한다() {
            assertThatThrownBy(() -> new BellEntity(null, BellType.AFTER_END, -1, 1))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_TIME.getMessage());
        }

        @ValueSource(ints = {0, BellEntity.MAX_BELL_COUNT + 1})
        @ParameterizedTest
        void 벨_횟수는_정해진_횟수_이내여야_한다(int count) {
            assertThatThrownBy(() -> new BellEntity(null, BellType.AFTER_END, 1, count))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_BELL_COUNT.getMessage());
        }
    }
}
