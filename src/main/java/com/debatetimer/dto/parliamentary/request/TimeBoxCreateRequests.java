package com.debatetimer.dto.parliamentary.request;

import com.debatetimer.dto.member.TableResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TimeBoxCreateRequests(

        @ArraySchema(schema = @Schema(description = "테이블 타임박스들", implementation = TimeBoxCreateRequest.class))
        List<TimeBoxCreateRequest> timeBoxCreateRequests
) {

}
