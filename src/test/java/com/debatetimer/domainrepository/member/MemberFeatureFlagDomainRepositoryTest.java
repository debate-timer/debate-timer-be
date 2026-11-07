package com.debatetimer.domainrepository.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.member.MemberFeatureFlags;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberFeatureFlagDomainRepositoryTest extends BaseDomainRepositoryTest {

    @Autowired
    private MemberFeatureFlagDomainRepository memberFeatureFlagDomainRepository;

    @Nested
    class GetFlags {

        @Test
        void 회원에게_부여된_플래그만_가져온다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            Member other = memberGenerator.generate("other@gmail.com");
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            MemberFeatureFlags bitoFlags = memberFeatureFlagDomainRepository.getFlags(bito.getId());
            MemberFeatureFlags otherFlags = memberFeatureFlagDomainRepository.getFlags(other.getId());

            assertAll(
                    () -> assertThat(bitoFlags.hasAll(FeatureFlag.TRANSCRIPT)).isTrue(),
                    () -> assertThat(otherFlags.hasAll(FeatureFlag.TRANSCRIPT)).isFalse()
            );
        }
    }
}
