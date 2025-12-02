package com.debatetimer.domain.organization;

import lombok.Getter;

@Getter
public class OrganizationTemplate {

    private final Long id;
    private final String name;
    private final String data;

    public OrganizationTemplate(Long id, String name, String data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }
}
