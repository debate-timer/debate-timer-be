package com.debatetimer.domainrepository.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.FeatureFlagGrant;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.member.MemberFeatureFlags;
import com.debatetimer.domainrepository.BaseDomainRepositoryTest;
import java.util.List;
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

    @Nested
    class Grant {

        @Test
        void 회원에게_플래그를_부여한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            memberFeatureFlagDomainRepository.grant(bito.getId(), FeatureFlag.TRANSCRIPT);

            assertThat(memberFeatureFlagDomainRepository.getFlags(bito.getId()).hasAll(FeatureFlag.TRANSCRIPT)).isTrue();
        }

        @Test
        void 이미_부여된_플래그를_다시_부여해도_한_번만_저장한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");

            memberFeatureFlagDomainRepository.grant(bito.getId(), FeatureFlag.TRANSCRIPT);
            memberFeatureFlagDomainRepository.grant(bito.getId(), FeatureFlag.TRANSCRIPT);

            assertThat(memberFeatureFlagDomainRepository.findGrants(bito.getId())).hasSize(1);
        }
    }

    @Nested
    class Revoke {

        @Test
        void 회원의_플래그를_회수한다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            memberFeatureFlagDomainRepository.revoke(bito.getId(), FeatureFlag.TRANSCRIPT);

            assertThat(memberFeatureFlagDomainRepository.findGrants(bito.getId())).isEmpty();
        }
    }

    @Nested
    class FindAllGrants {

        @Test
        void 모든_회원의_부여_내역을_회원_순으로_가져온다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            Member other = memberGenerator.generate("other@gmail.com");
            memberFeatureFlagEntityGenerator.generate(other, FeatureFlag.TRANSCRIPT);
            memberFeatureFlagEntityGenerator.generate(bito, FeatureFlag.TRANSCRIPT);

            List<FeatureFlagGrant> grants = memberFeatureFlagDomainRepository.findAllGrants();

            assertAll(
                    () -> assertThat(grants).extracting(FeatureFlagGrant::memberId)
                            .containsExactly(bito.getId(), other.getId()),
                    () -> assertThat(grants).allSatisfy(grant -> assertThat(grant.grantedAt()).isNotNull())
            );
        }
    }
}
