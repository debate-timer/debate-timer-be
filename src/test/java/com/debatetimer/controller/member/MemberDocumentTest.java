package com.debatetimer.controller.member;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.TableResponse;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.dto.member.TableType;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class MemberDocumentTest extends BaseDocumentTest {

    @Nested
    class CreateMember {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .description("멤버 생성")
                .requestBodyField(
                        fieldWithPath("nickname").type(STRING).description("멤버 닉네임")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("멤버 ID"),
                        fieldWithPath("nickname").type(STRING).description("멤버 닉네임")
                );

        @Test
        void 회원_생성_성공() {
            MemberCreateRequest request = new MemberCreateRequest("커찬");
            MemberCreateResponse response = new MemberCreateResponse(1L, "커찬");
            when(memberService.createMember(request)).thenReturn(response);

            var document = document("member/create", 201)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201);
        }

        @EnumSource(
                value = ClientErrorCode.class,
                names = {"INVALID_MEMBER_NICKNAME_LENGTH", "INVALID_MEMBER_NICKNAME_FORM"}
        )
        @ParameterizedTest
        void 회원_생성_실패(ClientErrorCode errorCode) {
            MemberCreateRequest request = new MemberCreateRequest("커찬");
            when(memberService.createMember(request)).thenThrow(new DTClientErrorException(errorCode));

            var document = document("member/create", errorCode)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }

    @Nested
    class GetTables {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.MEMBER_API)
                .description("멤버의 토론 시간표 조회")
                .queryParameter(
                        parameterWithName("memberId").description("멤버 ID")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("tables").type(ARRAY).description("멤버의 토론 테이블들"),
                        fieldWithPath("tables[].name").type(STRING).description("토론 테이블 이름"),
                        fieldWithPath("tables[].type").type(STRING).description("토론 타입"),
                        fieldWithPath("tables[].duration").type(NUMBER).description("소요 시간 (초)")
                );

        @Test
        void 테이블_조회_성공() {
            TableResponses response = new TableResponses(List.of(
                    new TableResponse("토론 테이블 1", TableType.PARLIAMENTARY, 1800),
                    new TableResponse("토론 테이블 2", TableType.PARLIAMENTARY, 2000)
            ));
            when(memberService.getTables(EXIST_MEMBER_ID)).thenReturn(response);

            var document = document("member/create", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", EXIST_MEMBER_ID)
                    .when().get("/api/table")
                    .then().statusCode(200);
        }



        @EnumSource(value = ClientErrorCode.class, names = {"MEMBER_NOT_FOUND"})
        @ParameterizedTest
        void 테이블_조회_실패(ClientErrorCode errorCode) {
            when(memberService.getTables(EXIST_MEMBER_ID)).thenThrow(new DTClientErrorException(errorCode));

            var document = document("member/create", 200)
                    .request(requestDocument)
                    .response(ERROR_RESPONSE)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", EXIST_MEMBER_ID)
                    .when().get("/api/table")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
