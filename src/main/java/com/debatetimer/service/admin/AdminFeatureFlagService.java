package com.debatetimer.service.admin;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.FeatureFlagGrant;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domainrepository.member.MemberDomainRepository;
import com.debatetimer.domainrepository.member.MemberFeatureFlagDomainRepository;
import com.debatetimer.dto.admin.response.AdminMemberFeatureFlagResponse;
import com.debatetimer.dto.admin.response.AdminMemberFeatureFlagsResponse;
import com.debatetimer.dto.admin.request.FeatureFlagGrantRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFeatureFlagService {

    private final MemberDomainRepository memberDomainRepository;
    private final MemberFeatureFlagDomainRepository memberFeatureFlagDomainRepository;

    public AdminMemberFeatureFlagsResponse findAllMembers() {
        Map<Long, List<FeatureFlagGrant>> memberIdToGrants = memberFeatureFlagDomainRepository.findAllGrants()
                .stream()
                .collect(Collectors.groupingBy(FeatureFlagGrant::memberId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, Member> idToMember = memberDomainRepository.findAllByIds(memberIdToGrants.keySet())
                .stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        List<AdminMemberFeatureFlagResponse> members = memberIdToGrants.entrySet()
                .stream()
                .filter(entry -> idToMember.containsKey(entry.getKey()))
                .map(entry -> new AdminMemberFeatureFlagResponse(idToMember.get(entry.getKey()), entry.getValue()))
                .toList();
        return new AdminMemberFeatureFlagsResponse(members);
    }

    public AdminMemberFeatureFlagResponse findMember(String email) {
        Member member = memberDomainRepository.getByEmail(email);
        return new AdminMemberFeatureFlagResponse(member, memberFeatureFlagDomainRepository.findGrants(member.getId()));
    }

    public AdminMemberFeatureFlagResponse grant(FeatureFlagGrantRequest request) {
        Member member = memberDomainRepository.getByEmail(request.email());
        memberFeatureFlagDomainRepository.grant(member.getId(), request.featureFlag());
        return new AdminMemberFeatureFlagResponse(member, memberFeatureFlagDomainRepository.findGrants(member.getId()));
    }

    public void revoke(long memberId, FeatureFlag featureFlag) {
        memberFeatureFlagDomainRepository.revoke(memberId, featureFlag);
    }
}
