package com.debatetimer.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberSettingsTest {

    @Nested
    class Defaults {

        @Test
        void 음량_기본값은_0_5_이다() {
            MemberSettings settings = MemberSettings.defaults(Language.US_EN);

            assertAll(
                    () -> assertThat(settings.getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(settings.getVolume()).isEqualByComparingTo("0.5")
            );
        }
    }

    @Nested
    class Update {

        private final MemberSettings settings = MemberSettings.defaults(Language.KO_KR);

        @Test
        void 언어와_음량을_변경한다() {
            MemberSettings updated = settings.update(Language.US_EN, 0.8);

            assertAll(
                    () -> assertThat(updated.getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(updated.getVolume()).isEqualByComparingTo("0.8")
            );
        }

        @Test
        void 언어가_null_이면_기존_언어를_유지한다() {
            MemberSettings updated = settings.update(null, 0.3);

            assertAll(
                    () -> assertThat(updated.getLanguage()).isEqualTo(Language.KO_KR),
                    () -> assertThat(updated.getVolume()).isEqualByComparingTo("0.3")
            );
        }

        @Test
        void 음량이_null_이면_기존_음량을_유지한다() {
            MemberSettings updated = settings.update(Language.US_EN, null);

            assertAll(
                    () -> assertThat(updated.getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(updated.getVolume()).isEqualByComparingTo("0.5")
            );
        }

        @Test
        void 언어와_음량이_모두_null_이면_예외를_던진다() {
            assertThatThrownBy(() -> settings.update(null, null))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EMPTY_MEMBER_SETTINGS.getMessage());
        }

        @ParameterizedTest
        @CsvSource({"0.55, 0.6", "0.54, 0.5", "0.06, 0.1", "0.0, 0.0", "1.0, 1.0"})
        void 음량은_소수점_한_자리로_반올림한다(double volume, String expected) {
            MemberSettings updated = settings.update(null, volume);

            assertThat(updated.getVolume()).isEqualTo(new BigDecimal(expected));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, 1.1, -0.01, 1.01})
        void 음량이_범위를_벗어나면_예외를_던진다(double volume) {
            assertThatThrownBy(() -> settings.update(null, volume))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_MEMBER_VOLUME.getMessage());
        }
    }
}
