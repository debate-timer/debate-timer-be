package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.dto.admin.MemberSignupStatsResponse;
import com.debatetimer.dto.admin.TableUsageStatsResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminStatsControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";

    @Nested
    class GetTableUsageStats {

        @Test
        void 어드민_비밀번호가_맞으면_단위별_테이블_통계를_조회한다() {
            TableUsageStatsResponse response = given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("unit", "WEEK")
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-14")
                    .when().get("/api/admin/tables/stats")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(TableUsageStatsResponse.class);

            assertThat(response.periods()).hasSize(3);
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .queryParam("unit", "DAY")
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-07")
                    .when().get("/api/admin/tables/stats")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 집계_단위가_잘못되면_조회할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("unit", "YEAR")
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-07")
                    .when().get("/api/admin/tables/stats")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class GetMemberSignupStats {

        @Test
        void 어드민_비밀번호가_맞으면_단위별_회원_통계를_조회한다() {
            MemberSignupStatsResponse response = given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("unit", "MONTH")
                    .queryParam("from", "2026-01-01")
                    .queryParam("to", "2026-12-31")
                    .when().get("/api/admin/members/stats")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(MemberSignupStatsResponse.class);

            assertThat(response.periods()).hasSize(12);
        }

        @Test
        void 어드민_비밀번호가_없으면_조회할_수_없다() {
            given()
                    .queryParam("unit", "DAY")
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-07")
                    .when().get("/api/admin/members/stats")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
