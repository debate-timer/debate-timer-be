package com.debatetimer.controller.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.OAuthToken;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.exception.ErrorResponse;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.NullAndEmptyAndBlankSource;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.http.HttpStatus;

class MemberControllerTest extends BaseControllerTest {

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "커스텀 테이블", "주제", "찬성", "반대", false, false);
            customizeTableRepository.save(table.toEntity());
            Headers headers = headerGenerator.generateAccessTokenHeader(member);

            TableResponses response = given()
                    .contentType(ContentType.JSON)
                    .headers(headers)
                    .when().get("/api/table")
                    .then().statusCode(200)
                    .extract().as(TableResponses.class);

            assertThat(response.tables()).hasSize(1);
        }
    }

    @Nested
    class CreateMember {

        @Test
        void 회원을_생성한다() {
            MemberCreateRequest request = new MemberCreateRequest("gnkldsnglnksl", "http://redirectUrl");

            sendMemberCreateRequest(request, HttpStatus.CREATED);
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 회원을_생성할_때_code에는_개행문자_외_다른_글자가_포함되야한다(String code) {
            MemberCreateRequest request = new MemberCreateRequest(code, "http://redirectUrl");

            ErrorResponse errorResponse = sendMemberCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptyAndBlankSource
        void 회원을_생성할_때_redirect주소에는_개행문자_외_다른_글자가_포함되야한다(String redirectUrl) {
            MemberCreateRequest request = new MemberCreateRequest("gnkldsnglnksl", redirectUrl);

            ErrorResponse errorResponse = sendMemberCreateRequest(request, HttpStatus.BAD_REQUEST)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.FIELD_ERROR.getMessage());
        }

        private ValidatableResponse sendMemberCreateRequest(
                MemberCreateRequest request,
                HttpStatus statusCode
        ) {
            OAuthToken oAuthToken = new OAuthToken("accessToken");
            MemberInfo memberInfo = new MemberInfo("default@gmail.com");
            doReturn(oAuthToken).when(oAuthClient).requestToken(request);
            doReturn(memberInfo).when(oAuthClient).requestMemberInfo(oAuthToken);

            return given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(statusCode.value());
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
