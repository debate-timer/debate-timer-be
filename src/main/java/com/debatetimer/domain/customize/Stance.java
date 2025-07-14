package com.debatetimer.domain.customize;

public enum Stance {

    PROS,
    CONS,
    NEUTRAL,
    ;

    public boolean isNeutralStance() {
        return this == NEUTRAL;
    }
}
