package com.debatetimer.domain.member;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MemberFeatureFlags {

    private final Set<FeatureFlag> flags;

    public MemberFeatureFlags(Collection<FeatureFlag> flags) {
        this.flags = flags.isEmpty() ? EnumSet.noneOf(FeatureFlag.class) : EnumSet.copyOf(flags);
    }

    // enum 선언 순서로 돌려준다
    public List<FeatureFlag> values() {
        return List.copyOf(flags);
    }

    public boolean hasAll(FeatureFlag... requiredFlags) {
        return flags.containsAll(Arrays.asList(requiredFlags));
    }
}
