package com.debatetimer.domainrepository.member;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.FeatureFlagGrant;
import com.debatetimer.domain.member.MemberFeatureFlags;
import com.debatetimer.entity.member.MemberFeatureFlagEntity;
import com.debatetimer.repository.member.MemberFeatureFlagRepository;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<FeatureFlagGrant> findGrants(long memberId) {
        return memberFeatureFlagRepository.findAllByMemberIdOrderByIdAsc(memberId)
                .stream()
                .map(MemberFeatureFlagEntity::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagGrant> findAllGrants() {
        return memberFeatureFlagRepository.findAllByOrderByMemberIdAscIdAsc()
                .stream()
                .map(MemberFeatureFlagEntity::toDomain)
                .toList();
    }

    // 이미 부여된 플래그면 아무것도 하지 않는다
    @Transactional
    public void grant(long memberId, FeatureFlag featureFlag) {
        if (memberFeatureFlagRepository.existsByMemberIdAndFeatureFlag(memberId, featureFlag)) {
            return;
        }
        memberFeatureFlagRepository.save(new MemberFeatureFlagEntity(memberId, featureFlag));
    }

    @Transactional
    public void revoke(long memberId, FeatureFlag featureFlag) {
        memberFeatureFlagRepository.deleteByMemberIdAndFeatureFlag(memberId, featureFlag);
    }
}
