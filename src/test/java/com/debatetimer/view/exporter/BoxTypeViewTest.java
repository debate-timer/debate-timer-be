package com.debatetimer.view.exporter;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.debatetimer.domain.BoxType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BoxTypeViewTest {

    @Nested
    class MapView {

        @ParameterizedTest
        @EnumSource(value = BoxType.class)
        void 타임박스_타입과_일치하는_메시지를_반환한다(BoxType boxType) {
            assertThatCode(() -> BoxTypeView.mapView(boxType))
                    .doesNotThrowAnyException();
        }
    }
}
