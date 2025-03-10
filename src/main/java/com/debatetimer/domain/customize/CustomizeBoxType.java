package com.debatetimer.domain.customize;

public enum CustomizeBoxType {

    NORMAL,
    TIME_BASED;

    public boolean isTimeBased() {
        return this == TIME_BASED;
    }

    public boolean isNotTimeBased() {
        return !isTimeBased();
    }
}
