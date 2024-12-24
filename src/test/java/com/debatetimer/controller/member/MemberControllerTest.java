package com.debatetimer.controller.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.BaseControllerTest;
import com.debatetimer.controller.member.dto.MemberCreateRequest;
import com.debatetimer.controller.member.dto.MemberCreateResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberControllerTest extends BaseControllerTest {

    @Nested
    class CreateMember {

        @Test
        void 회원를_생성한다() {
            MemberCreateRequest request = new MemberCreateRequest("커찬");

            MemberCreateResponse response = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().log().all()
                    .statusCode(201)
                    .extract().as(MemberCreateResponse.class);

            assertThat(response.nickname()).isEqualTo("커찬");
        }
    }
}
