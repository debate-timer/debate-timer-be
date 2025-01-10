package com.debatetimer.view.exporter;

import com.debatetimer.domain.BoxType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoxTypeView {
    OPENING_VIEW(BoxType.OPENING, "입론"),
    REBUTTAL_VIEW(BoxType.REBUTTAL, "반론"),
    CROSS(BoxType.CROSS, "교차 질의"),
    CLOSING(BoxType.CLOSING, "최종 발언"),
    TIME_OUT(BoxType.TIME_OUT, "작전 시간"),
    ;

    private final BoxType boxType;
    private final String viewMessage;

    public static String mapView(BoxType target) {
        return Stream.of(values())
                .filter(value -> value.boxType == target)
                .findAny()
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TYPE))
                .viewMessage;
    }
}
