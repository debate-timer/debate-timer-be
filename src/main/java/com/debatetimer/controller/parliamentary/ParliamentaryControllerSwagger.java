package com.debatetimer.controller.parliamentary;

import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.swagger.annotation.ErrorCode400;
import com.debatetimer.swagger.annotation.ErrorCode404;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ParliamentaryTable API")
public interface ParliamentaryControllerSwagger {

    @Operation(
            summary = "새로운 의회식 토론 시간표 생성",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "의회식 토론 시간표 생성 성공",
                            content = @Content(schema = @Schema(implementation = ParliamentaryTableResponse.class))
                    )
            }
    )
    @ErrorCode400
    ParliamentaryTableResponse save(
            ParliamentaryTableCreateRequest tableCreateRequest,
            @Parameter(hidden = true) Member member
    );

    @Operation(
            summary = "의회식 토론 시간표 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토론 시간표 조회 성공",
                            content = @Content(schema = @Schema(implementation = ParliamentaryTableResponse.class))
                    )
            }
    )
    @ErrorCode400
    @ErrorCode404(description = "회원 테이블이 없는 경우")
    ParliamentaryTableResponse getTable(
            Long tableId,
            @Parameter(hidden = true) Member member
    );

    @Operation(
            summary = "의회식 토론 시간표 수정",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "의회식 토론 시간표 수정 성공",
                            content = @Content(schema = @Schema(implementation = ParliamentaryTableResponse.class))
                    )
            }
    )
    @ErrorCode400
    @ErrorCode404(description = "회원 테이블이 없는 경우")
    ParliamentaryTableResponse updateTable(
            ParliamentaryTableCreateRequest tableCreateRequest,
            Long tableId,
            @Parameter(hidden = true) Member member
    );

    @Operation(
            summary = "의회식 토론 시간표 삭제",
            responses = @ApiResponse(responseCode = "204", description = "의회식 토론 시간표 삭제 성공")
    )
    @ErrorCode400
    @ErrorCode404(description = "회원 테이블이 없는 경우")
    void deleteTable(
            Long tableId,
            @Parameter(hidden = true) Member member
    );
}
