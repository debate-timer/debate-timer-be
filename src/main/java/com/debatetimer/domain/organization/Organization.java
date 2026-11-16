package com.debatetimer.domain.organization;

import java.util.List;
import lombok.Getter;

@Getter
public class Organization {

    private final Long id;
    private final String name;
    private final String affiliation;
    private final String iconPath;
    private final Language language;
    private final OrganizationCategory category;
    private final List<OrganizationTemplate> templates;

    public Organization(Long id,
                        String name,
                        String affiliation,
                        String iconPath,
                        Language language,
                        OrganizationCategory category,
                        List<OrganizationTemplate> templates) {
        this.id = id;
        this.name = name;
        this.affiliation = affiliation;
        this.iconPath = iconPath;
        this.language = language;
        this.category = category;
        this.templates = templates;
    }

    public Organization(String name,
                        String affiliation,
                        String iconPath,
                        Language language,
                        OrganizationCategory category) {
        this(null, name, affiliation, iconPath, language, category, List.of());
    }
}
