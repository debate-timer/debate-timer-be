package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.organization.Language;
import com.debatetimer.dto.admin.AdminOrganizationResponse;
import com.debatetimer.dto.admin.AdminOrganizationTemplateResponse;
import com.debatetimer.dto.admin.AdminOrganizationsResponse;
import com.debatetimer.dto.admin.request.OrganizationCreateRequest;
import com.debatetimer.dto.admin.request.OrganizationTemplateCreateRequest;
import com.debatetimer.entity.organization.OrganizationEntity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminOrganizationControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";

    @Nested
    class GetOrganizations {

        @Test
        void 어드민_비밀번호가_맞으면_모든_단체와_템플릿을_조회한다() {
            OrganizationEntity korean = organizationEntityGenerator.generate("한앎", "한양대", Language.KO_KR);
            organizationEntityGenerator.generate("english", "hanyang", Language.US_EN);
            organizationTemplateEntityGenerator.generate(korean, "템플릿");

            AdminOrganizationsResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/organizations")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(AdminOrganizationsResponse.class);

            assertAll(
                    () -> assertThat(response.organizations()).hasSize(2),
                    () -> assertThat(response.organizations().get(0).templates()).hasSize(1)
            );
        }

        @Test
        void 어드민_비밀번호가_틀리면_조회할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, "wrong-password")
                    .when().get("/api/admin/organizations")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class CreateOrganization {

        @Test
        void 단체를_추가한다() {
            OrganizationCreateRequest request = new OrganizationCreateRequest(
                    "한앎", "한양대", "/icon.png", Language.KO_KR);

            AdminOrganizationResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/admin/organizations")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .extract().as(AdminOrganizationResponse.class);

            assertAll(
                    () -> assertThat(response.name()).isEqualTo("한앎"),
                    () -> assertThat(response.templates()).isEmpty()
            );
        }

        @Test
        void 단체_이름이_비어_있으면_추가할_수_없다() {
            OrganizationCreateRequest request = new OrganizationCreateRequest(
                    " ", "한양대", "/icon.png", Language.KO_KR);

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/admin/organizations")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 어드민_비밀번호가_없으면_추가할_수_없다() {
            OrganizationCreateRequest request = new OrganizationCreateRequest(
                    "한앎", "한양대", "/icon.png", Language.KO_KR);

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/api/admin/organizations")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class CreateTemplate {

        @Test
        void 단체에_템플릿을_추가한다() {
            OrganizationEntity organization = organizationEntityGenerator.generate("한앎", "한양대");
            OrganizationTemplateCreateRequest request = new OrganizationTemplateCreateRequest("템플릿", "data");

            AdminOrganizationTemplateResponse response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .pathParam("organizationId", organization.getId())
                    .body(request)
                    .when().post("/api/admin/organizations/{organizationId}/templates")
                    .then().statusCode(HttpStatus.CREATED.value())
                    .extract().as(AdminOrganizationTemplateResponse.class);

            assertThat(response.name()).isEqualTo("템플릿");
        }

        @Test
        void 단체가_없으면_추가할_수_없다() {
            OrganizationTemplateCreateRequest request = new OrganizationTemplateCreateRequest("템플릿", "data");

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .pathParam("organizationId", 999)
                    .body(request)
                    .when().post("/api/admin/organizations/{organizationId}/templates")
                    .then().statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
