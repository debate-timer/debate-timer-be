package com.debatetimer.service.admin;

import com.debatetimer.domain.stats.StatPeriod;
import com.debatetimer.domain.stats.StatUnit;
import com.debatetimer.domainrepository.customize.CustomizeTableDomainRepository;
import com.debatetimer.dto.admin.TableUsageStatResponse;
import com.debatetimer.dto.admin.TableUsageStatsResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 테이블 생성 시각(created_at)과 마지막 사용 시각(used_at)을 집계 단위별로 센다.
 * 사용 시각은 테이블마다 마지막 사용 한 번만 남으므로, 사용 횟수는 그 구간에 마지막으로 쓰인 테이블 수다.
 */
@Service
@RequiredArgsConstructor
public class TableUsageStatsService {

    private final CustomizeTableDomainRepository customizeTableDomainRepository;

    public TableUsageStatsResponse getStats(StatUnit unit, LocalDate from, LocalDate to) {
        StatPeriod period = new StatPeriod(unit, from, to);
        Map<LocalDate, Long> createdCounts = period.countByPeriod(
                customizeTableDomainRepository.findCreatedAtBetween(period.getStartAt(), period.getEndAt()));
        Map<LocalDate, Long> usedCounts = period.countByPeriod(
                customizeTableDomainRepository.findUsedAtBetween(period.getStartAt(), period.getEndAt()));

        List<TableUsageStatResponse> periods = period.getPeriodStarts()
                .stream()
                .map(periodStart -> new TableUsageStatResponse(
                        periodStart,
                        createdCounts.getOrDefault(periodStart, 0L),
                        usedCounts.getOrDefault(periodStart, 0L)
                )).toList();
        return new TableUsageStatsResponse(unit, period.getStart(), period.getEnd().minusDays(1), periods);
    }
}
