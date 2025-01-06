package com.debatetimer.controller.parliamentary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.BaseDocumentTest;
import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.Stance;
import com.debatetimer.dto.parliamentary.request.ParliamentaryTableCreateRequest;
import com.debatetimer.dto.parliamentary.request.TableInfoCreateRequest;
import com.debatetimer.dto.parliamentary.request.TimeBoxCreateRequest;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class ParliamentaryDocumentTest extends BaseDocumentTest {

    @Nested
    class Save {

        @ParameterizedTest
        @EnumSource(value = ClientErrorCode.class, names = {"INVALID_TABLE_TIME"})
        void 토론_테이블_생성_예외_처리(ClientErrorCode errorCode) {
            ParliamentaryTableCreateRequest request = new ParliamentaryTableCreateRequest(
                    new TableInfoCreateRequest("비토 테이블", "주제"),
                    List.of(
                            new TimeBoxCreateRequest(Stance.PROS, BoxType.OPENING, 120, 1),
                            new TimeBoxCreateRequest(Stance.CONS, BoxType.OPENING, 120, 1)
                    )
            );
            when(parliamentaryService.save(eq(request), any())).thenThrow(new DTClientErrorException(errorCode));

            var document = document("table/create/" + errorCode.name())
                    .tag("Parliamentary Table API")
                    .queryParameter(
                            parameterWithName("memberId").description("멤버 ID")
                    )
                    .requestBodyField(
                            fieldWithPath("info").type(OBJECT).description("토론 테이블 정보"),
                            fieldWithPath("info.name").type(STRING).description("테이블 이름"),
                            fieldWithPath("info.agenda").type(STRING).description("토론 주제"),
                            fieldWithPath("table").type(ARRAY).description("토론 테이블 구성"),
                            fieldWithPath("table[].stance").type(STRING).description("입장"),
                            fieldWithPath("table[].type").type(STRING).description("발언 유형"),
                            fieldWithPath("table[].time").type(NUMBER).description("발언 시간(초)"),
                            fieldWithPath("table[].speakerNumber").type(NUMBER).description("발언자 번호").optional()
                    )
                    .responseBodyField(
                            fieldWithPath("message").type(STRING).description("에러 메시지")
                    )
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", EXIST_MEMBER_ID)
                    .body(request)
                    .when().post("/api/table/parliamentary")
                    .then().statusCode(errorCode.getStatus().value());
        }
    }
}
