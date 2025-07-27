package com.debatetimer.domain.customize;

import java.util.function.Predicate;

public enum BellType {

    AFTER_START(time -> time >= 0),
    BEFORE_END(time -> time >= 0),
    AFTER_END(time -> time <= 0),
    ;

    private final Predicate<Integer> timeValidator;

    BellType(Predicate<Integer> timeValidator) {
        this.timeValidator = timeValidator;
    }

    public boolean isValidTime(int time) {
        return timeValidator.test(time);
    }
}
