package com.debatetimer.service.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domainrepository.organization.OrganizationDomainRepository;
import com.debatetimer.dto.organization.OrganizationResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationDomainRepository organizationDomainRepository;

    public OrganizationResponses findAll(Language language) {
        return OrganizationResponses.from(organizationDomainRepository.findAllByLanguage(language));
    }
}
