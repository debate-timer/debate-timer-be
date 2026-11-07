package com.debatetimer.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberFeatureFlagsTest {

    @Nested
    class HasAll {

        @Test
        void 요구한_플래그를_모두_가지면_참이다() {
            MemberFeatureFlags flags = new MemberFeatureFlags(List.of(FeatureFlag.TRANSCRIPT));

            assertThat(flags.hasAll(FeatureFlag.TRANSCRIPT)).isTrue();
        }

        @Test
        void 요구한_플래그가_없으면_거짓이다() {
            MemberFeatureFlags flags = new MemberFeatureFlags(List.of());

            assertThat(flags.hasAll(FeatureFlag.TRANSCRIPT)).isFalse();
        }

        @Test
        void 요구한_플래그가_없으면_항상_참이다() {
            MemberFeatureFlags flags = new MemberFeatureFlags(List.of());

            assertThat(flags.hasAll()).isTrue();
        }
    }
}
