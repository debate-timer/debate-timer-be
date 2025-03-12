package com.debatetimer.domain.customize;

public enum CustomizeBoxType { //TODO 자유토론 박스 둘로 나누기

    NORMAL,
    TIME_BASED;

    public boolean isTimeBased() {
        return this == TIME_BASED;
    }

    public boolean isNotTimeBased() {
        return !isTimeBased();
    }
}
