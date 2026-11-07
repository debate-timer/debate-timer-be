package com.debatetimer.controller.admin;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.dto.admin.response.AdminFeatureFlagsResponse;
import com.debatetimer.dto.admin.response.AdminMemberFeatureFlagResponse;
import com.debatetimer.dto.admin.response.AdminMemberFeatureFlagsResponse;
import com.debatetimer.dto.admin.request.FeatureFlagGrantRequest;
import com.debatetimer.service.admin.AdminFeatureFlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminFeatureFlagController {

    private final AdminFeatureFlagService adminFeatureFlagService;

    @GetMapping("/api/admin/feature-flags")
    @ResponseStatus(HttpStatus.OK)
    public AdminFeatureFlagsResponse getFeatureFlags() {
        return AdminFeatureFlagsResponse.all();
    }

    @GetMapping("/api/admin/feature-flags/members")
    @ResponseStatus(HttpStatus.OK)
    public AdminMemberFeatureFlagsResponse getMembers() {
        return adminFeatureFlagService.findAllMembers();
    }

    @GetMapping("/api/admin/feature-flags/members/search")
    @ResponseStatus(HttpStatus.OK)
    public AdminMemberFeatureFlagResponse searchMember(@RequestParam String email) {
        return adminFeatureFlagService.findMember(email);
    }

    @PostMapping("/api/admin/feature-flags/members")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminMemberFeatureFlagResponse grant(@Valid @RequestBody FeatureFlagGrantRequest request) {
        return adminFeatureFlagService.grant(request);
    }

    @DeleteMapping("/api/admin/feature-flags/members/{memberId}/{featureFlag}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable long memberId, @PathVariable FeatureFlag featureFlag) {
        adminFeatureFlagService.revoke(memberId, featureFlag);
    }
}
