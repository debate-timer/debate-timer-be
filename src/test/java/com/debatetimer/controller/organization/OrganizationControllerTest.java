package com.debatetimer.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.dto.organization.OrganizationResponses;
import com.debatetimer.entity.organization.OrganizationEntity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class OrganizationControllerTest extends BaseControllerTest {

    @Nested
    class GetOrganizationTemplates {

        @Test
        void 모든_기관의_토론_템플릿을_조회할_수_있다() {
            OrganizationEntity organization1 = organizationEntityGenerator.generate("한앎", "한양대");
            OrganizationEntity organization2 = organizationEntityGenerator.generate("한모름", "양한대");
            organizationTemplateEntityGenerator.generate(organization1, "템플릿1");
            organizationTemplateEntityGenerator.generate(organization1, "템플릿2");
            organizationTemplateEntityGenerator.generate(organization2, "릿플템1");

            OrganizationResponses response = given()
                    .contentType(ContentType.JSON)
                    .when().get("/api/organizations/templates")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(OrganizationResponses.class);

            assertAll(
                    () -> assertThat(response.organizations()).hasSize(2),
                    () -> assertThat(response.organizations().get(0).templates()).hasSize(2),
                    () -> assertThat(response.organizations().get(1).templates()).hasSize(1)
            );
        }
    }
}
