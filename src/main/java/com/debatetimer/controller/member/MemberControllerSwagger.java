package com.debatetimer.controller.member;

import com.debatetimer.controller.member.dto.MemberCreateRequest;
import com.debatetimer.controller.member.dto.MemberCreateResponse;
import com.debatetimer.controller.member.dto.TableResponses;
import com.debatetimer.swagger.annotation.ErrorCode400;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member API")
public interface MemberControllerSwagger {

    @Operation(
            summary = "멤버의 토론 시간표 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "멤버의 토론 시간표 조회 성공",
                            content = @Content(schema = @Schema(implementation = TableResponses.class))
                    )
            }
    )
    @ErrorCode400
    TableResponses getTables(Long memberId);

    @Operation(
            summary = "멤버 생성",
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = MemberCreateRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "멤버 생성 성공",
                            content = @Content(schema = @Schema(implementation = MemberCreateResponse.class))
                    )
            }
    )
    @ErrorCode400
    MemberCreateResponse createMember(MemberCreateRequest request);
}
