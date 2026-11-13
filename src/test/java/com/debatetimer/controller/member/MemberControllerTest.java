package com.debatetimer.controller.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeTable;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Language;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.MemberSettingsResponse;
import com.debatetimer.dto.member.MemberSettingsUpdateRequest;
import com.debatetimer.dto.member.OAuthToken;
import com.debatetimer.dto.member.MemberFeatureFlagsResponse;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.ErrorResponse;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.NullAndEmptyAndBlankSource;
import com.debatetimer.repository.member.MemberRepository;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.ValidatableResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class MemberControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    class GetTables {

        @Test
        void 회원의_전체_토론_시간표를_조회한다() {
            Member member = memberGenerator.generate("default@gmail.com");
            CustomizeTable table = new CustomizeTable(member, "커스텀 테이블", "주제", "찬성", "반대", false, false,
                    LocalDateTime.now());
            customizeTableRepository.save(new CustomizeTableEntity(table));
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
    class GetFeatureFlags {

        @Test
        void 회원의_피쳐_플래그를_조회한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            MemberFeatureFlagsResponse response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .when().get("/api/member/feature-flags")
                    .then().statusCode(200)
                    .extract().as(MemberFeatureFlagsResponse.class);

            assertThat(response.featureFlags()).containsExactly(FeatureFlag.TRANSCRIPT);
        }

        @Test
        void 플래그가_없으면_빈_목록을_조회한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            MemberFeatureFlagsResponse response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .when().get("/api/member/feature-flags")
                    .then().statusCode(200)
                    .extract().as(MemberFeatureFlagsResponse.class);

            assertThat(response.featureFlags()).isEmpty();
        }

        @Test
        void 로그인하지_않으면_조회할_수_없다() {
            given()
                    .when().get("/api/member/feature-flags")
                    .then().statusCode(401);
        }
    }

    @Nested
    class GetSettings {

        @Test
        void 회원의_설정을_조회한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            MemberSettingsResponse response = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .when().get("/api/member/settings")
                    .then().statusCode(200)
                    .extract().as(MemberSettingsResponse.class);

            assertThat(response).isEqualTo(new MemberSettingsResponse(Language.KO_KR, new BigDecimal("0.5")));
        }

        @Test
        void 언어는_클라이언트_언어_코드로_응답한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            String language = given()
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .when().get("/api/member/settings")
                    .then().statusCode(200)
                    .extract().path("language");

            assertThat(language).isEqualTo("ko");
        }

        @Test
        void 로그인하지_않으면_조회할_수_없다() {
            given()
                    .when().get("/api/member/settings")
                    .then().statusCode(401);
        }
    }

    @Nested
    class UpdateSettings {

        @Test
        void 회원의_설정을_변경한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            MemberSettingsUpdateRequest request = new MemberSettingsUpdateRequest(Language.US_EN, 0.3);

            MemberSettingsResponse response = given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .body(request)
                    .when().patch("/api/member/settings")
                    .then().statusCode(200)
                    .extract().as(MemberSettingsResponse.class);

            assertThat(response).isEqualTo(new MemberSettingsResponse(Language.US_EN, new BigDecimal("0.3")));
        }

        @Test
        void 클라이언트_언어_코드로_변경하고_응답한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            String language = given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .body(Map.of("language", "en"))
                    .when().patch("/api/member/settings")
                    .then().statusCode(200)
                    .extract().path("language");

            assertAll(
                    () -> assertThat(language).isEqualTo("en"),
                    () -> assertThat(memberRepository.getById(bito.getId()).getSettings().getLanguage())
                            .isEqualTo(Language.US_EN)
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"EN", "US_EN", "ja"})
        void 지원하지_않는_언어_코드면_400_에러를_반환한다(String language) {
            Member bito = memberGenerator.generate("bito@gmail.com");

            ErrorResponse errorResponse = given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .body(Map.of("language", language))
                    .when().patch("/api/member/settings")
                    .then().statusCode(400)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.MESSAGE_NOT_READABLE.getMessage());
        }

        @Test
        void 지원하지_않는_언어면_400_에러를_반환한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            ErrorResponse errorResponse = given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .body(Map.of("language", "EN", "volume", 0.5))
                    .when().patch("/api/member/settings")
                    .then().statusCode(400)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.MESSAGE_NOT_READABLE.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, 1.1})
        void 음량이_범위를_벗어나면_400_에러를_반환한다(double volume) {
            Member bito = memberGenerator.generate("bito@gmail.com");
            MemberSettingsUpdateRequest request = new MemberSettingsUpdateRequest(null, volume);

            ErrorResponse errorResponse = given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .body(request)
                    .when().patch("/api/member/settings")
                    .then().statusCode(400)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.INVALID_MEMBER_VOLUME.getMessage());
        }

        @Test
        void 변경할_설정이_없으면_400_에러를_반환한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            ErrorResponse errorResponse = given()
                    .contentType(ContentType.JSON)
                    .headers(headerGenerator.generateAccessTokenHeader(bito))
                    .body(Map.of())
                    .when().patch("/api/member/settings")
                    .then().statusCode(400)
                    .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo(ClientErrorCode.EMPTY_MEMBER_SETTINGS.getMessage());
        }

        @Test
        void 로그인하지_않으면_변경할_수_없다() {
            given()
                    .contentType(ContentType.JSON)
                    .body(new MemberSettingsUpdateRequest(Language.US_EN, 0.3))
                    .when().patch("/api/member/settings")
                    .then().statusCode(401);
        }
    }

    @Nested
    class CreateMember {

        @Test
        void 영어_브라우저로_가입하면_언어를_US_EN_으로_설정한다() {
            MemberCreateRequest request = new MemberCreateRequest("gnkldsnglnksl", "http://redirectUrl");
            OAuthToken oAuthToken = new OAuthToken("accessToken");
            doReturn(oAuthToken).when(oAuthClient).requestToken(request);
            doReturn(new MemberInfo("default@gmail.com")).when(oAuthClient).requestMemberInfo(oAuthToken);

            given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                    .body(request)
                    .when().post("/api/member")
                    .then().statusCode(201);

            Member member = memberRepository.getByEmail("default@gmail.com");
            assertThat(member.getSettings().getLanguage()).isEqualTo(Language.US_EN);
        }

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
