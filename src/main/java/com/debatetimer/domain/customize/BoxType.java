package com.debatetimer.domain.customize;

public enum BoxType {

    NORMAL,
    TIME_BASED;

    public boolean isTimeBased() {
        return this == TIME_BASED;
    }

    public boolean isNotTimeBased() {
        return !isTimeBased();
    }
}
