package com.debatetimer.fixture.entity;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.member.MemberFeatureFlagEntity;
import com.debatetimer.repository.member.MemberFeatureFlagRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberFeatureFlagEntityGenerator {

    private final MemberFeatureFlagRepository memberFeatureFlagRepository;

    public MemberFeatureFlagEntityGenerator(MemberFeatureFlagRepository memberFeatureFlagRepository) {
        this.memberFeatureFlagRepository = memberFeatureFlagRepository;
    }

    public MemberFeatureFlagEntity generate(Member member, FeatureFlag featureFlag) {
        return memberFeatureFlagRepository.save(new MemberFeatureFlagEntity(member.getId(), featureFlag));
    }
}
