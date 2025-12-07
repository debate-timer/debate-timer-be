package com.debatetimer.service;

import com.debatetimer.domainrepository.organization.OrganizationDomainRepository;
import com.debatetimer.dto.organization.OrganizationsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationDomainRepository organizationDomainRepository;

    public OrganizationsResponse findAll() {
        return OrganizationsResponse.from(organizationDomainRepository.findAll());
    }
}
