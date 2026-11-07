package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.admin.AdminFeatureFlagsResponse;
import com.debatetimer.dto.admin.AdminGrantedFeatureFlagResponse;
import com.debatetimer.dto.admin.AdminMemberFeatureFlagResponse;
import com.debatetimer.dto.admin.AdminMemberFeatureFlagsResponse;
import com.debatetimer.dto.admin.request.FeatureFlagGrantRequest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminFeatureFlagControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";

    @Nested
    class GetFeatureFlags {

        @Test
        void 정의된_피쳐_플래그_목록을_조회한다() {
            AdminFeatureFlagsResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/feature-flags")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminFeatureFlagsResponse.class);

            assertThat(response.featureFlags()).hasSize(FeatureFlag.values().length);
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .when().get("/api/admin/feature-flags")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class GetMembers {

        @Test
        void 플래그를_가진_회원만_조회한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            memberGenerator.generate("other@gmail.com");
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            AdminMemberFeatureFlagsResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/feature-flags/members")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminMemberFeatureFlagsResponse.class);

            assertAll(
                    () -> assertThat(response.members()).hasSize(1),
                    () -> assertThat(response.members().get(0).email()).isEqualTo("bito@gmail.com"),
                    () -> assertThat(response.members().get(0).featureFlags())
                            .extracting(AdminGrantedFeatureFlagResponse::featureFlag)
                            .containsExactly(FeatureFlag.TRANSCRIPT)
            );
        }
    }

    @Nested
    class SearchMember {

        @Test
        void 이메일로_회원의_플래그를_조회한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            AdminMemberFeatureFlagResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("email", "bito@gmail.com")
                    .when().get("/api/admin/feature-flags/members/search")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminMemberFeatureFlagResponse.class);

            assertAll(
                    () -> assertThat(response.memberId()).isEqualTo(bito.getId()),
                    () -> assertThat(response.featureFlags()).isEmpty()
            );
        }

        @Test
        void 없는_회원이면_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("email", "none@gmail.com")
                    .when().get("/api/admin/feature-flags/members/search")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class Grant {

        @Test
        void 이메일로_회원에게_플래그를_부여한다() {
            memberGenerator.generate("bito@gmail.com");

            AdminMemberFeatureFlagResponse response = grant(
                    new FeatureFlagGrantRequest("bito@gmail.com", FeatureFlag.TRANSCRIPT), HttpStatus.CREATED);

            assertThat(response.featureFlags())
                    .extracting(AdminGrantedFeatureFlagResponse::featureFlag)
                    .containsExactly(FeatureFlag.TRANSCRIPT);
        }

        @Test
        void 이미_있는_플래그를_다시_부여해도_성공한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            AdminMemberFeatureFlagResponse response = grant(
                    new FeatureFlagGrantRequest("bito@gmail.com", FeatureFlag.TRANSCRIPT), HttpStatus.CREATED);

            assertThat(response.featureFlags()).hasSize(1);
        }

        @Test
        void 없는_회원에게는_부여할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(new FeatureFlagGrantRequest("none@gmail.com", FeatureFlag.TRANSCRIPT))
                    .when().post("/api/admin/feature-flags/members")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 없는_플래그는_부여할_수_없다() {
            memberGenerator.generate("bito@gmail.com");

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "bito@gmail.com", "featureFlag", "UNKNOWN"))
                    .when().post("/api/admin/feature-flags/members")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        private AdminMemberFeatureFlagResponse grant(FeatureFlagGrantRequest request, HttpStatus status) {
            return given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/admin/feature-flags/members")
                    .then().statusCode(status.value())
                    .extract().as(AdminMemberFeatureFlagResponse.class);
        }
    }

    @Nested
    class Revoke {

        @Test
        void 회원의_플래그를_회수한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .pathParam("memberId", bito.getId())
                    .pathParam("featureFlag", FeatureFlag.TRANSCRIPT)
                    .when().delete("/api/admin/feature-flags/members/{memberId}/{featureFlag}")
                    .then().statusCode(HttpStatus.NO_CONTENT.value());

            AdminMemberFeatureFlagsResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/feature-flags/members")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminMemberFeatureFlagsResponse.class);
            assertThat(response.members()).isEmpty();
        }
    }
}
