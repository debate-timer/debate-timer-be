package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.dto.admin.SharingStatsResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminSharingControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";

    @Nested
    class GetSharingStats {

        @Test
        void 어드민_비밀번호가_맞으면_기간별_통계를_조회한다() {
            SharingStatsResponse response = given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-07")
                    .when().get("/api/admin/sharing/stats")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(SharingStatsResponse.class);

            assertThat(response.days()).hasSize(7);
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-07")
                    .when().get("/api/admin/sharing/stats")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 어드민_비밀번호가_없으면_조회할_수_없다() {
            given()
                    .queryParam("from", "2026-10-01")
                    .queryParam("to", "2026-10-07")
                    .when().get("/api/admin/sharing/stats")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        void 조회_기간이_잘못되면_조회할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("from", "2026-10-07")
                    .queryParam("to", "2026-10-01")
                    .when().get("/api/admin/sharing/stats")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}
