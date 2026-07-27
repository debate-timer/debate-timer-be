package com.debatetimer.domainrepository.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.organization.OrganizationEntity;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrganizationDomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private OrganizationDomainRepository organizationDomainRepository;

    @Nested
    class FindAllByLanguage {

        @Test
        void 해당_언어의_모든_조직_템플릿을_가져온다() {
            OrganizationEntity organization1 = organizationEntityGenerator.generate("한앎", "한양대", Language.KR);
            OrganizationEntity organization2 = organizationEntityGenerator.generate("한모름", "양한대", Language.KR);
            organizationTemplateEntityGenerator.generate(organization1, "템플릿1", Language.KR);
            organizationTemplateEntityGenerator.generate(organization1, "템플릿2", Language.KR);
            organizationTemplateEntityGenerator.generate(organization2, "릿플템1", Language.KR);

            List<Organization> organizations = organizationDomainRepository.findAllByLanguage(Language.KR);

            assertAll(
                    () -> assertThat(organizations).hasSize(2),
                    () -> assertThat(organizations.get(0).getTemplates()).hasSize(2),
                    () -> assertThat(organizations.get(1).getTemplates()).hasSize(1)
            );
        }

        @Test
        void 요청한_언어의_조직_템플릿만_반환한다() {
            OrganizationEntity korean = organizationEntityGenerator.generate("한앎", "한양대", Language.KR);
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.EN);
            organizationTemplateEntityGenerator.generate(korean, "템플릿1", Language.KR);
            organizationTemplateEntityGenerator.generate(english, "template1", Language.EN);
            organizationTemplateEntityGenerator.generate(english, "template2", Language.EN);

            List<Organization> englishOrganizations = organizationDomainRepository.findAllByLanguage(Language.EN);

            assertAll(
                    () -> assertThat(englishOrganizations).hasSize(1),
                    () -> assertThat(englishOrganizations.get(0).getName()).isEqualTo("english"),
                    () -> assertThat(englishOrganizations.get(0).getTemplates()).hasSize(2)
            );
        }
    }
}
