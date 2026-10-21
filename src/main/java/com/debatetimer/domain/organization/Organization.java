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
    private final List<OrganizationTemplate> templates;

    public Organization(Long id,
                        String name,
                        String affiliation,
                        String iconPath,
                        Language language,
                        List<OrganizationTemplate> templates) {
        this.id = id;
        this.name = name;
        this.affiliation = affiliation;
        this.iconPath = iconPath;
        this.language = language;
        this.templates = templates;
    }

    public Organization(String name, String affiliation, String iconPath, Language language) {
        this(null, name, affiliation, iconPath, language, List.of());
    }
}
