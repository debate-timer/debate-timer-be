package com.debatetimer.controller.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.TableResponses;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberControllerTest extends BaseControllerTest {

    @Nested
    class CreateMember {

        @Test
        void 회원을_생성한다() {
            MemberCreateRequest request = new MemberCreateRequest("커찬");

            MemberCreateResponse response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201)
                    .extract().as(MemberCreateResponse.class);

            assertThat(response.nickname()).isEqualTo(request.nickname());
        }
    }

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberRepository.save(new Member("커찬"));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 A", "주제", 1800, false, false));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 B", "주제", 1900, false, false));

            TableResponses response = given()
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", member.getId())
                    .when().get("/api/table")
                    .then().statusCode(200)
                    .extract().as(TableResponses.class);

            assertThat(response.tables()).hasSize(2);
        }
    }
}
