package com.debatetimer.service.admin;

import com.debatetimer.domainrepository.organization.OrganizationDomainRepository;
import com.debatetimer.dto.admin.AdminOrganizationResponse;
import com.debatetimer.dto.admin.AdminOrganizationTemplateResponse;
import com.debatetimer.dto.admin.AdminOrganizationsResponse;
import com.debatetimer.dto.admin.request.OrganizationCreateRequest;
import com.debatetimer.dto.admin.request.OrganizationTemplateCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOrganizationService {

    private final OrganizationDomainRepository organizationDomainRepository;

    public AdminOrganizationsResponse findAll() {
        return AdminOrganizationsResponse.from(organizationDomainRepository.findAll());
    }

    public AdminOrganizationResponse create(OrganizationCreateRequest request) {
        return new AdminOrganizationResponse(organizationDomainRepository.create(request.toOrganization()));
    }

    public AdminOrganizationTemplateResponse createTemplate(long organizationId,
                                                            OrganizationTemplateCreateRequest request) {
        return new AdminOrganizationTemplateResponse(
                organizationDomainRepository.createTemplate(organizationId, request.toTemplate()));
    }
}
