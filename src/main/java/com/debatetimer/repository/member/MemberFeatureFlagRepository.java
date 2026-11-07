package com.debatetimer.repository.member;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.entity.member.MemberFeatureFlagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MemberFeatureFlagRepository extends Repository<MemberFeatureFlagEntity, Long> {

    MemberFeatureFlagEntity save(MemberFeatureFlagEntity memberFeatureFlagEntity);

    @Query("SELECT f.featureFlag FROM MemberFeatureFlagEntity f WHERE f.memberId = :memberId")
    List<FeatureFlag> findFeatureFlagsByMemberId(long memberId);
}
