package com.debatetimer.domain.organization;

import java.util.List;

public class Organization {

    private final Long id;
    private final String name;
    private final String affiliation;
    private final String iconPath;
    private final List<OrganizationTemplate> templates;

    public Organization(Long id,
                        String name,
                        String affiliation,
                        String iconPath,
                        List<OrganizationTemplate> templates) {
        this.id = id;
        this.name = name;
        this.affiliation = affiliation;
        this.iconPath = iconPath;
        this.templates = templates;
    }
}
