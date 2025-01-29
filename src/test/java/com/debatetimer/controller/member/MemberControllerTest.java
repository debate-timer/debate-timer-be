package com.debatetimer.controller.member;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

class MemberControllerTest extends BaseControllerTest {

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberGenerator.generate("커찬");
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 A", "주제", 1800));
            parliamentaryTableRepository.save(new ParliamentaryTable(member, "토론 시간표 B", "주제", 1900));

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
            MemberCreateRequest request = new MemberCreateRequest("gnkldsnglnksl");
            OAuthToken oAuthToken = new OAuthToken("accessToken");
            MemberInfo memberInfo = new MemberInfo("비토");
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
            Member bito = memberGenerator.generate("비토");
            String refreshToken = tokenGenerator.generateRefreshToken(bito.getNickname());

            given()
                    .cookie("refreshToken", refreshToken)
                    .when().post("/api/member/reissue")
                    .then().statusCode(200);
        }
    }

    @Nested
    class Logout {

        @Test
        void 로그아웃한다() {
            Member bito = memberGenerator.generate("비토");
            Headers headers = headerGenerator.generateAccessTokenHeader(bito);
            String refreshToken = tokenGenerator.generateRefreshToken(bito.getNickname());

            given()
                    .cookie("refreshToken", refreshToken)
                    .headers(headers)
                    .when().post("/api/member/logout")
                    .then().statusCode(204);
        }
    }
}
