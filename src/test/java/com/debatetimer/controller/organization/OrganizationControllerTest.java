package com.debatetimer.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.organization.Language;
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
        void language_파라미터가_없으면_KR_기관의_토론_템플릿을_조회한다() {
            OrganizationEntity korean1 = organizationEntityGenerator.generate("한앎", "한양대", Language.KR);
            OrganizationEntity korean2 = organizationEntityGenerator.generate("한모름", "양한대", Language.KR);
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.EN);
            organizationTemplateEntityGenerator.generate(korean1, "템플릿1", Language.KR);
            organizationTemplateEntityGenerator.generate(korean1, "템플릿2", Language.KR);
            organizationTemplateEntityGenerator.generate(korean2, "릿플템1", Language.KR);
            organizationTemplateEntityGenerator.generate(english, "template1", Language.EN);

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

        @Test
        void 요청한_언어의_기관_토론_템플릿만_조회한다() {
            OrganizationEntity korean = organizationEntityGenerator.generate("한앎", "한양대", Language.KR);
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.EN);
            organizationTemplateEntityGenerator.generate(korean, "템플릿1", Language.KR);
            organizationTemplateEntityGenerator.generate(english, "template1", Language.EN);
            organizationTemplateEntityGenerator.generate(english, "template2", Language.EN);

            OrganizationResponses response = given()
                    .contentType(ContentType.JSON)
                    .queryParam("language", "EN")
                    .when().get("/api/organizations/templates")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(OrganizationResponses.class);

            assertAll(
                    () -> assertThat(response.organizations()).hasSize(1),
                    () -> assertThat(response.organizations().get(0).organization()).isEqualTo("english"),
                    () -> assertThat(response.organizations().get(0).templates()).hasSize(2)
            );
        }

        @Test
        void 지원하지_않는_언어를_요청하면_400을_반환한다() {
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("language", "JP")
                    .when().get("/api/organizations/templates")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}
