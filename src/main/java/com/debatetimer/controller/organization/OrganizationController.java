package com.debatetimer.controller.organization;

import com.debatetimer.dto.organization.OrganizationResponses;
import com.debatetimer.service.organization.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/api/organizations/templates")
    public ResponseEntity<OrganizationResponses> getOrganizationTemplates() {
        return ResponseEntity.ok(organizationService.findAll());
    }
}
