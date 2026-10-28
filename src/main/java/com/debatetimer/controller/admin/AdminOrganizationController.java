package com.debatetimer.controller.admin;

import com.debatetimer.dto.admin.AdminOrganizationResponse;
import com.debatetimer.dto.admin.AdminOrganizationTemplateResponse;
import com.debatetimer.dto.admin.AdminOrganizationsResponse;
import com.debatetimer.dto.admin.request.OrganizationCreateRequest;
import com.debatetimer.dto.admin.request.OrganizationTemplateCreateRequest;
import com.debatetimer.service.admin.AdminOrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminOrganizationController {

    private final AdminOrganizationService adminOrganizationService;

    @GetMapping("/api/admin/organizations")
    @ResponseStatus(HttpStatus.OK)
    public AdminOrganizationsResponse getOrganizations() {
        return adminOrganizationService.findAll();
    }

    @PostMapping("/api/admin/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminOrganizationResponse createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request
    ) {
        return adminOrganizationService.create(request);
    }

    @PostMapping("/api/admin/organizations/{organizationId}/templates")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminOrganizationTemplateResponse createTemplate(
            @PathVariable long organizationId,
            @Valid @RequestBody OrganizationTemplateCreateRequest request
    ) {
        return adminOrganizationService.createTemplate(organizationId, request);
    }
}
