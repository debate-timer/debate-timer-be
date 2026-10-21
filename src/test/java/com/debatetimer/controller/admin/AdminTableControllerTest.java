package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.admin.AdminTableDetailResponse;
import com.debatetimer.dto.admin.AdminTablePageResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminTableControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";

    @Nested
    class GetTables {

        @Test
        void 어드민_비밀번호가_맞으면_테이블_목록을_조회한다() {
            Member member = memberGenerator.generate("member@email.com");
            customizeTableEntityGenerator.generate(member);
            customizeTableEntityGenerator.generate(member);

            AdminTablePageResponse response = given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("sort", "USED_AT")
                    .queryParam("page", 0)
                    .queryParam("size", 1)
                    .when().get("/api/admin/tables")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminTablePageResponse.class);

            assertThat(response.totalElements()).isEqualTo(2);
        }

        @Test
        void 정렬_기준이_잘못되면_조회할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .queryParam("sort", "NAME")
                    .when().get("/api/admin/tables")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .when().get("/api/admin/tables")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class GetTable {

        @Test
        void 어드민_비밀번호가_맞으면_테이블_상세를_조회한다() {
            Member member = memberGenerator.generate("member@email.com");
            CustomizeTableEntity table = customizeTableEntityGenerator.generate(member);
            customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);

            AdminTableDetailResponse response = given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .pathParam("tableId", table.getId())
                    .when().get("/api/admin/tables/{tableId}")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminTableDetailResponse.class);

            assertThat(response.table()).hasSize(1);
        }

        @Test
        void 어드민_비밀번호가_없으면_조회할_수_없다() {
            given()
                    .pathParam("tableId", 1)
                    .when().get("/api/admin/tables/{tableId}")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
