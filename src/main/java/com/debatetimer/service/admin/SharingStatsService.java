package com.debatetimer.service.admin;

import com.debatetimer.domain.sharing.SharingLog;
import com.debatetimer.domainrepository.sharing.SharingLogDomainRepository;
import com.debatetimer.dto.admin.SharingDailyStatResponse;
import com.debatetimer.dto.admin.SharingStatsResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 종료(FINISHED)된 공유를 시작일 기준으로 모아 일별 횟수와 평균 공유 시간을 계산한다.
 */
@Service
@RequiredArgsConstructor
public class SharingStatsService {

    public static final int MAX_PERIOD_DAYS = 366;
    private static final double SECONDS_PER_MINUTE = 60.0;

    private final SharingLogDomainRepository sharingLogDomainRepository;

    public SharingStatsResponse getDailyStats(LocalDate from, LocalDate to) {
        validatePeriod(from, to);
        Map<LocalDate, List<SharingLog>> logsByDate = sharingLogDomainRepository
                .findAllFinishedStartedBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(sharingLog -> sharingLog.getStartedAt().toLocalDate()));

        List<SharingDailyStatResponse> days = from.datesUntil(to.plusDays(1))
                .map(date -> toDailyStat(date, logsByDate.getOrDefault(date, List.of())))
                .toList();
        return new SharingStatsResponse(from, to, days);
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) >= MAX_PERIOD_DAYS) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_STATS_PERIOD);
        }
    }

    private SharingDailyStatResponse toDailyStat(LocalDate date, List<SharingLog> logs) {
        double averageMinutes = logs.stream()
                .mapToLong(SharingLog::getDurationSeconds)
                .average()
                .orElse(0) / SECONDS_PER_MINUTE;
        return new SharingDailyStatResponse(date, logs.size(), Math.round(averageMinutes * 10) / 10.0);
    }
}
