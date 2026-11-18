package com.debatetimer.domainrepository.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationCategory;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
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
            OrganizationEntity organization1 = organizationEntityGenerator.generate("한앎", "한양대", Language.KO_KR);
            OrganizationEntity organization2 = organizationEntityGenerator.generate("한모름", "양한대", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(organization1, "템플릿1", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(organization1, "템플릿2", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(organization2, "릿플템1", Language.KO_KR);

            List<Organization> organizations = organizationDomainRepository.findAllByLanguage(Language.KO_KR);

            assertAll(
                    () -> assertThat(organizations).hasSize(2),
                    () -> assertThat(organizations.get(0).getTemplates()).hasSize(2),
                    () -> assertThat(organizations.get(1).getTemplates()).hasSize(1)
            );
        }

        @Test
        void 요청한_언어의_조직_템플릿만_반환한다() {
            OrganizationEntity korean = organizationEntityGenerator.generate("한앎", "한양대", Language.KO_KR);
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.US_EN);
            organizationTemplateEntityGenerator.generate(korean, "템플릿1", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(english, "template1", Language.US_EN);
            organizationTemplateEntityGenerator.generate(english, "template2", Language.US_EN);

            List<Organization> englishOrganizations = organizationDomainRepository.findAllByLanguage(Language.US_EN);

            assertAll(
                    () -> assertThat(englishOrganizations).hasSize(1),
                    () -> assertThat(englishOrganizations.get(0).getName()).isEqualTo("english"),
                    () -> assertThat(englishOrganizations.get(0).getTemplates()).hasSize(2)
            );
        }
    }

    @Nested
    class FindAll {

        @Test
        void 언어와_관계없이_모든_단체와_템플릿을_가져온다() {
            OrganizationEntity korean = organizationEntityGenerator.generate("한앎", "한양대", Language.KO_KR);
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.US_EN);
            organizationTemplateEntityGenerator.generate(korean, "템플릿1", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(english, "template1", Language.US_EN);

            List<Organization> organizations = organizationDomainRepository.findAll();

            assertAll(
                    () -> assertThat(organizations).extracting(Organization::getName)
                            .containsExactly("한앎", "english"),
                    () -> assertThat(organizations.get(1).getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(organizations.get(1).getTemplates()).hasSize(1)
            );
        }
    }

    @Nested
    class Create {

        @Test
        void 단체를_저장한다() {
            Organization saved = organizationDomainRepository.create(
                    new Organization("한앎", "한양대", "/icon.png", Language.KO_KR, OrganizationCategory.DEBATE_CLUB));

            assertAll(
                    () -> assertThat(saved.getId()).isNotNull(),
                    () -> assertThat(saved.getCategory()).isEqualTo(OrganizationCategory.DEBATE_CLUB),
                    () -> assertThat(organizationDomainRepository.findAllByLanguage(Language.KO_KR)).hasSize(1)
            );
        }
    }

    @Nested
    class Update {

        @Test
        void 단체의_모든_항목을_수정한다() {
            OrganizationEntity organization = organizationEntityGenerator.generate("한앎", "한양대");

            Organization updated = organizationDomainRepository.update(organization.getId(),
                    new Organization("hanalm", "hanyang", "/new-icon.png", Language.US_EN,
                            OrganizationCategory.TOURNAMENT));

            assertAll(
                    () -> assertThat(updated.getId()).isEqualTo(organization.getId()),
                    () -> assertThat(updated.getName()).isEqualTo("hanalm"),
                    () -> assertThat(updated.getAffiliation()).isEqualTo("hanyang"),
                    () -> assertThat(updated.getIconPath()).isEqualTo("/new-icon.png"),
                    () -> assertThat(updated.getLanguage()).isEqualTo(Language.US_EN),
                    () -> assertThat(updated.getCategory()).isEqualTo(OrganizationCategory.TOURNAMENT)
            );
        }

        @Test
        void 언어를_바꾸면_템플릿도_바뀐_언어로_조회된다() {
            OrganizationEntity organization = organizationEntityGenerator.generate("한앎", "한양대", Language.KO_KR);
            organizationTemplateEntityGenerator.generate(organization, "템플릿1", Language.KO_KR);

            organizationDomainRepository.update(organization.getId(),
                    new Organization("한앎", "한양대", "/icon.png", Language.US_EN, OrganizationCategory.GLOBAL));

            List<Organization> englishOrganizations = organizationDomainRepository.findAllByLanguage(Language.US_EN);
            assertAll(
                    () -> assertThat(englishOrganizations).hasSize(1),
                    () -> assertThat(englishOrganizations.get(0).getTemplates()).extracting(OrganizationTemplate::getName)
                            .containsExactly("템플릿1"),
                    () -> assertThat(organizationDomainRepository.findAllByLanguage(Language.KO_KR)).isEmpty()
            );
        }

        @Test
        void 단체가_없으면_예외를_던진다() {
            Organization organization = new Organization("한앎", "한양대", "/icon.png", Language.KO_KR,
                    OrganizationCategory.DEBATE_CLUB);

            assertThatThrownBy(() -> organizationDomainRepository.update(999L, organization))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.ORGANIZATION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class CreateTemplate {

        @Test
        void 템플릿을_단체와_같은_언어로_저장한다() {
            OrganizationEntity english = organizationEntityGenerator.generate("english", "hanyang", Language.US_EN);

            organizationDomainRepository.createTemplate(english.getId(), new OrganizationTemplate("template", "data"));

            List<Organization> organizations = organizationDomainRepository.findAllByLanguage(Language.US_EN);
            assertThat(organizations.get(0).getTemplates()).extracting(OrganizationTemplate::getName)
                    .containsExactly("template");
        }

        @Test
        void 단체가_없으면_예외를_던진다() {
            OrganizationTemplate template = new OrganizationTemplate("template", "data");

            assertThatThrownBy(() -> organizationDomainRepository.createTemplate(999L, template))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.ORGANIZATION_NOT_FOUND.getMessage());
        }
    }
}
