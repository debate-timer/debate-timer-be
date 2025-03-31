package com.debatetimer.controller.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OAuthToken;
import com.debatetimer.dto.member.TableResponses;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberControllerTest extends BaseControllerTest {

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 A", "주제", false, false));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 B", "주제", false, false));

            Headers headers = headerGenerator.generateAccessTokenHeader(member);

            TableResponses response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .when().get("/api/table")
                    .then().statusCode(200)
                    .extract().as(TableResponses.class);

            assertThat(response.tables()).hasSize(2);
        }
    }

    @Nested
    class CreateMember {

        @Test
        void 회원을_생성한다() {
            MemberCreateRequest request = new MemberCreateRequest("gnkldsnglnksl", "http://redirectUrl");
            OAuthToken oAuthToken = new OAuthToken("accessToken");
            MemberInfo memberInfo = new MemberInfo("default@gmail.com");
            doReturn(oAuthToken).when(oAuthClient).requestToken(request);
            doReturn(memberInfo).when(oAuthClient).requestMemberInfo(oAuthToken);

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201);
        }
    }

    @Nested
    class ReissueAccessToken {

        @Test
        void 토큰을_갱신한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            String refreshToken = tokenGenerator.generateRefreshToken(bito.getEmail());

            given()
                    .cookie("refreshToken", refreshToken)
                    .when().post("/api/member/reissue")
                    .then().statusCode(200);
        }

        @Test
        void 토큰이_없을_경우_400_에러를_반환한다() {
            given()
                    .when().post("/api/member/reissue")
                    .then().statusCode(400);
        }
    }

    @Nested
    class Logout {

        @Test
        void 로그아웃한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);
            String refreshToken = tokenGenerator.generateRefreshToken(bito.getEmail());

            given()
                    .cookie("refreshToken", refreshToken)
                    .headers(headers)
                    .when().post("/api/member/logout")
                    .then().statusCode(204);
        }

        @Test
        void 토큰이_없을_경우_400_에러를_반환한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);

            given()
                    .headers(headers)
                    .when().post("/api/member/logout")
                    .then().statusCode(400);
        }
    }
}
