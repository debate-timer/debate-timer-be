package com.debatetimer.view.exporter;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ParliamentaryBoxTypeViewTest {

    @Nested
    class MapView {

        @ParameterizedTest
        @EnumSource(value = ParliamentaryBoxType.class)
        void 타임박스_타입과_일치하는_메시지를_반환한다(ParliamentaryBoxType boxType) {
            assertThatCode(() -> ParliamentaryBoxTypeView.mapView(boxType))
                    .doesNotThrowAnyException();
        }
    }
}
