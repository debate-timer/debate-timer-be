package com.debatetimer.fixture.entity;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.OrganizationCategory;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.repository.organization.OrganizationRepository;
import org.springframework.stereotype.Component;

@Component
public class OrganizationEntityGenerator {

    private static final String DEFAULT_ICON_PATH = "/static/icons/default_icon.png";

    private final OrganizationRepository organizationRepository;

    public OrganizationEntityGenerator(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public OrganizationEntity generate(String name, String affiliation) {
        return generate(name, affiliation, Language.KO_KR);
    }

    public OrganizationEntity generate(String name, String affiliation, Language language) {
        return generate(name, affiliation, language, OrganizationCategory.CLASSIC);
    }

    public OrganizationEntity generate(String name,
                                       String affiliation,
                                       Language language,
                                       OrganizationCategory category) {
        OrganizationEntity organization = new OrganizationEntity(
                name, affiliation, DEFAULT_ICON_PATH, language, category);
        return organizationRepository.save(organization);
    }
}
