package com.debatetimer.entity.member;

import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.FeatureFlagGrant;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member_feature_flag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "feature_flag"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFeatureFlagEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private long memberId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "feature_flag", length = 50)
    private FeatureFlag featureFlag;

    public MemberFeatureFlagEntity(long memberId, FeatureFlag featureFlag) {
        this.memberId = memberId;
        this.featureFlag = featureFlag;
    }

    public FeatureFlagGrant toDomain() {
        return new FeatureFlagGrant(memberId, featureFlag, getCreatedAt());
    }
}
