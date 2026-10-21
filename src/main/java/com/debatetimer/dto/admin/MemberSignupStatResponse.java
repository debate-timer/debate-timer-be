package com.debatetimer.dto.admin;

import jakarta.annotation.Nullable;
import java.time.LocalDate;

/**
 * @param growthRate 구간 시작 전 회원 수 대비 신규 회원 비율(%). 시작 전 회원이 없으면 비울 수 없어 null
 */
public record MemberSignupStatResponse(
        LocalDate periodStart,
        long newMemberCount,
        long totalMemberCount,
        @Nullable Double growthRate
) {
}
