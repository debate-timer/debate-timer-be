package com.debatetimer.domain.stats;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.function.UnaryOperator;

/**
 * 통계 집계 단위. 주간은 월요일, 월별은 1일을 구간의 시작일로 본다.
 */
public enum StatUnit {

    DAY(date -> date, date -> date.plusDays(1)),
    WEEK(date -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), date -> date.plusWeeks(1)),
    MONTH(date -> date.withDayOfMonth(1), date -> date.plusMonths(1)),
    ;

    private final UnaryOperator<LocalDate> truncator;
    private final UnaryOperator<LocalDate> nextFinder;

    StatUnit(UnaryOperator<LocalDate> truncator, UnaryOperator<LocalDate> nextFinder) {
        this.truncator = truncator;
        this.nextFinder = nextFinder;
    }

    public LocalDate truncate(LocalDate date) {
        return truncator.apply(date);
    }

    public LocalDate next(LocalDate periodStart) {
        return nextFinder.apply(periodStart);
    }
}
