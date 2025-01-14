package com.debatetimer.view.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.BoxType;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class BoxTypeViewTest {

    @Nested
    class MapView {

        @ParameterizedTest
        @EnumSource(value = BoxTypeView.class)
        void 타임박스_타입과_일치하는_메시지를_반환한다(BoxTypeView boxTypeView) {
            BoxType mappedBoxType = boxTypeView.getBoxType();
            String expectedMessage = boxTypeView.getViewMessage();

            String actualMappedMessage = BoxTypeView.mapView(mappedBoxType);

            assertThat(actualMappedMessage)
                    .isEqualTo(expectedMessage);
        }

        @Test
        void 일치하는_타임박스_타입이_없을_경우_에러를_발생시킨다() {
            BoxType mockBoxType = Mockito.mock(BoxType.class);

            assertThatThrownBy(() -> BoxTypeView.mapView(mockBoxType))
                    .isInstanceOf(DTServerErrorException.class)
                    .hasMessage(ServerErrorCode.EXCEL_EXPORT_ERROR.getMessage());
        }
    }
}
