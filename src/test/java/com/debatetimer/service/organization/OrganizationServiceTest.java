package com.debatetimer.service.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.dto.organization.OrganizationResponses;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.service.BaseServiceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrganizationServiceTest extends BaseServiceTest {

    @Autowired
    private OrganizationService organizationService;

    @Nested
    class FindAll {

        @Test
        void 저장한_모든_기관_템플릿을_반환한다() {
            OrganizationEntity organization1 = organizationEntityGenerator.generate("한앎", "한양대");
            OrganizationEntity organization2 = organizationEntityGenerator.generate("한모름", "양한대");
            organizationTemplateEntityGenerator.generate(organization1, "템플릿1");
            organizationTemplateEntityGenerator.generate(organization1, "템플릿2");
            organizationTemplateEntityGenerator.generate(organization2, "릿플템1");

            OrganizationResponses response = organizationService.findAll(Language.KO_KR);

            assertAll(
                    () -> assertThat(response.organizations()).hasSize(2),
                    () -> assertThat(response.organizations().get(0).templates()).hasSize(2),
                    () -> assertThat(response.organizations().get(1).templates()).hasSize(1)
            );
        }

        @Test
        void 요청한_언어의_기관_템플릿만_반환한다() {
            OrganizationEntity korean = organizationEntityGenerator.generate("한앎", "한양대", Language.KO_KR);
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.US_EN);
            organizationTemplateEntityGenerator.generate(korean, "템플릿1", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(english, "template1", Language.US_EN);

            OrganizationResponses response = organizationService.findAll(Language.US_EN);

            assertAll(
                    () -> assertThat(response.organizations()).hasSize(1),
                    () -> assertThat(response.organizations().get(0).organization()).isEqualTo("english")
            );
        }

        @Test
        void 비어있을_경우_빈_객체를_반환한다() {
            OrganizationResponses response = organizationService.findAll(Language.KO_KR);

            assertThat(response.organizations()).isEmpty();
        }
    }
}
