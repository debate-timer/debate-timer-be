package com.debatetimer.service.admin;

import com.debatetimer.domain.stats.StatPeriod;
import com.debatetimer.domain.stats.StatUnit;
import com.debatetimer.domainrepository.member.MemberDomainRepository;
import com.debatetimer.dto.admin.MemberSignupStatResponse;
import com.debatetimer.dto.admin.MemberSignupStatsResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 구간별 신규 가입 수와, 구간 시작 전 누적 회원 대비 증가 비율을 계산한다.
 */
@Service
@RequiredArgsConstructor
public class MemberSignupStatsService {

    private static final double PERCENT = 100.0;

    private final MemberDomainRepository memberDomainRepository;

    public MemberSignupStatsResponse getStats(StatUnit unit, LocalDate from, LocalDate to) {
        StatPeriod period = new StatPeriod(unit, from, to);
        Map<LocalDate, Long> newMemberCounts = period.countByPeriod(
                memberDomainRepository.findCreatedAtBetween(period.getStartAt(), period.getEndAt()));

        long totalMemberCount = memberDomainRepository.countCreatedBefore(period.getStartAt());
        List<MemberSignupStatResponse> periods = new ArrayList<>();
        for (LocalDate periodStart : period.getPeriodStarts()) {
            long newMemberCount = newMemberCounts.getOrDefault(periodStart, 0L);
            Double growthRate = calculateGrowthRate(newMemberCount, totalMemberCount);
            totalMemberCount += newMemberCount;
            periods.add(new MemberSignupStatResponse(periodStart, newMemberCount, totalMemberCount, growthRate));
        }
        return new MemberSignupStatsResponse(unit, period.getStart(), period.getEnd().minusDays(1), periods);
    }

    private Double calculateGrowthRate(long newMemberCount, long previousMemberCount) {
        if (previousMemberCount == 0) {
            return null;
        }
        return Math.round(newMemberCount * PERCENT / previousMemberCount * 10) / 10.0;
    }
}
