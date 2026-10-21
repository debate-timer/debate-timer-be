package com.debatetimer.domain.stats;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * 조회 기간을 집계 단위로 나눈 구간들. 시작일과 종료일이 걸친 구간 전체를 포함하도록 경계를 넓힌다.
 */
@Getter
public class StatPeriod {

    public static final int MAX_PERIOD_COUNT = 366;

    private final StatUnit unit;
    private final LocalDate start;
    private final LocalDate end;

    public StatPeriod(StatUnit unit, LocalDate from, LocalDate to) {
        validate(unit, from, to);
        this.unit = unit;
        this.start = unit.truncate(from);
        this.end = unit.next(unit.truncate(to));
        validatePeriodCount();
    }

    private void validate(StatUnit unit, LocalDate from, LocalDate to) {
        if (unit == null || from == null || to == null || from.isAfter(to)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_STATS_RANGE);
        }
    }

    private void validatePeriodCount() {
        if (periodStarts().limit(MAX_PERIOD_COUNT + 1L).count() > MAX_PERIOD_COUNT) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_STATS_RANGE);
        }
    }

    public LocalDateTime getStartAt() {
        return start.atStartOfDay();
    }

    public LocalDateTime getEndAt() {
        return end.atStartOfDay();
    }

    public List<LocalDate> getPeriodStarts() {
        return periodStarts().toList();
    }

    private Stream<LocalDate> periodStarts() {
        return Stream.iterate(start, date -> date.isBefore(end), unit::next);
    }

    public Map<LocalDate, Long> countByPeriod(List<LocalDateTime> times) {
        return times.stream()
                .filter(this::contains)
                .collect(Collectors.groupingBy(time -> unit.truncate(time.toLocalDate()), Collectors.counting()));
    }

    private boolean contains(LocalDateTime time) {
        return !time.isBefore(getStartAt()) && time.isBefore(getEndAt());
    }
}
