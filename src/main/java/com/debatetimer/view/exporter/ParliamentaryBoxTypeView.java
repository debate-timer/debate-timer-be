package com.debatetimer.view.exporter;

import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParliamentaryBoxTypeView {
    OPENING_VIEW(ParliamentaryBoxType.OPENING, "입론"),
    REBUTTAL_VIEW(ParliamentaryBoxType.REBUTTAL, "반론"),
    CROSS(ParliamentaryBoxType.CROSS, "교차 질의"),
    CLOSING(ParliamentaryBoxType.CLOSING, "최종 발언"),
    TIME_OUT(ParliamentaryBoxType.TIME_OUT, "작전 시간"),
    ;

    private final ParliamentaryBoxType boxType;
    private final String viewMessage;

    public static String mapView(ParliamentaryBoxType target) {
        return Stream.of(values())
                .filter(value -> value.boxType == target)
                .findAny()
                .orElseThrow(() -> new DTServerErrorException(ServerErrorCode.EXCEL_EXPORT_ERROR))
                .viewMessage;
    }
}
