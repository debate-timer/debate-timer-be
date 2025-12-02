package com.debatetimer.domainrepository.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
    class FindAll {

        @Test
        void 모든_조직_템플릿을_가져온다() {
            OrganizationEntity organization1 = organizationEntityGenerator.generate("한앎", "한양대");
            OrganizationEntity organization2 = organizationEntityGenerator.generate("한모름", "양한대");
            organizationTemplateEntityGenerator.generate(organization1, "템플릿1");
            organizationTemplateEntityGenerator.generate(organization1, "템플릿2");
            organizationTemplateEntityGenerator.generate(organization2, "릿플템1");

            List<Organization> organizations = organizationDomainRepository.findAll();

            assertAll(
                    () -> assertThat(organizations).hasSize(2),
                    () -> assertThat(organizations.get(0).getTemplates()).hasSize(2),
                    () -> assertThat(organizations.get(1).getTemplates()).hasSize(1)
            );
        }
    }
}
