package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record TimeBoxCreateRequests(

        @ArraySchema(schema = @Schema(description = "테이블 타임박스들", implementation = TimeBoxCreateRequest.class))
        List<TimeBoxCreateRequest> timeBoxCreateRequests
) {

    public ParliamentaryTimeBoxes toTimeBoxes(ParliamentaryTable parliamentaryTable) {
        return IntStream.range(0, timeBoxCreateRequests.size()) // 0부터 시작
                .mapToObj(i -> timeBoxCreateRequests.get(i).toTimeBox(parliamentaryTable, i + 1)) // i + 1로 순번 처리
                .collect(Collectors.collectingAndThen(Collectors.toList(), ParliamentaryTimeBoxes::new));
    }

    public int sumOfTime() {
        return timeBoxCreateRequests
                .stream()
                .mapToInt(TimeBoxCreateRequest::time)
                .sum();
    }
}
