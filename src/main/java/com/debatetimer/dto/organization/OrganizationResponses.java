package com.debatetimer.dto.organization;

import com.debatetimer.domain.organization.Organization;
import java.util.List;

public record OrganizationResponses(List<OrganizationResponse> organizations) {

    public static OrganizationResponses from(List<Organization> organizations) {
        return new OrganizationResponses(toOrganizationsResponse(organizations));
    }

    private static List<OrganizationResponse> toOrganizationsResponse(List<Organization> organizations) {
        return organizations.stream()
                .map(OrganizationResponse::new)
                .toList();
    }
}
