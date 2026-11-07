package com.debatetimer.domainrepository.member;

import com.debatetimer.domain.member.MemberFeatureFlags;
import com.debatetimer.repository.member.MemberFeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MemberFeatureFlagDomainRepository {

    private final MemberFeatureFlagRepository memberFeatureFlagRepository;

    @Transactional(readOnly = true)
    public MemberFeatureFlags getFlags(long memberId) {
        return new MemberFeatureFlags(memberFeatureFlagRepository.findFeatureFlagsByMemberId(memberId));
    }
}
