package com.debatetimer.service.organization;

import com.debatetimer.domainrepository.organization.OrganizationDomainRepository;
import com.debatetimer.dto.organization.OrganizationResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationDomainRepository organizationDomainRepository;

    public OrganizationResponses findAll() {
        return OrganizationResponses.from(organizationDomainRepository.findAll());
    }
}
