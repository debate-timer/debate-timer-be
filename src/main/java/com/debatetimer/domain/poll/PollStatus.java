package com.debatetimer.domain.poll;

public enum PollStatus {

    PROGRESS,
    DONE,
    ;

    public boolean isProgress() {
        return this == PROGRESS;
    }
}
