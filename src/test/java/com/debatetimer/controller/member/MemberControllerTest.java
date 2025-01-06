package com.debatetimer.controller.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.BaseControllerTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.TableResponses;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.restassured.RestDocumentationFilter;

class MemberControllerTest extends BaseControllerTest {

    @Nested
    class CreateMember {

        private final RestDocumentationFilter document = document("member/create")
                .tag("Member API")
                .description("멤버 생성")
                .requestBodyField(
                        fieldWithPath("nickname").type(STRING).description("멤버 닉네임")
                )
                .responseBodyField(
                        fieldWithPath("id").type(NUMBER).description("멤버 ID"),
                        fieldWithPath("nickname").type(STRING).description("멤버 닉네임")
                )
                .build();

        @Test
        void 회원을_생성한다() {
            MemberCreateRequest request = new MemberCreateRequest("커찬");

            MemberCreateResponse response = given(document)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201)
                    .extract().as(MemberCreateResponse.class);

            assertThat(response.nickname()).isEqualTo(request.nickname());
        }
    }

    @Nested
    class getTables {

        private final RestDocumentationFilter document = document("member/table")
                .tag("Member API")
                .description("멤버의 토론 시간표 조회")
                .queryParameter(
                        parameterWithName("memberId").description("멤버 ID")
                ).responseBodyField(
                        fieldWithPath("tables").type(ARRAY).description("멤버의 토론 테이블들"),
                        fieldWithPath("tables[].name").type(STRING).description("토론 테이블 이름"),
                        fieldWithPath("tables[].type").type(STRING).description("토론 타입"),
                        fieldWithPath("tables[].duration").type(NUMBER).description("소요 시간 (초)")
                )
                .build();

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberRepository.save(new Member("커찬"));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 A", "주제", 1800));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 B", "주제", 1900));

            TableResponses response = given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", member.getId())
                    .when().get("/api/table")
                    .then().statusCode(200)
                    .extract().as(TableResponses.class);

            assertThat(response.tables()).hasSize(2);
        }
    }
}
