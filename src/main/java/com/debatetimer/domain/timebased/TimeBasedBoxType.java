package com.debatetimer.domain.timebased;

import com.debatetimer.domain.Stance;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TimeBasedBoxType {

    OPENING(Set.of(Stance.PROS, Stance.CONS)),
    REBUTTAL(Set.of(Stance.PROS, Stance.CONS)),
    CROSS(Set.of(Stance.PROS, Stance.CONS)),
    CLOSING(Set.of(Stance.PROS, Stance.CONS)),
    TIME_BASED(Set.of(Stance.NEUTRAL)),
    LEADING(Set.of(Stance.PROS, Stance.CONS)),
    TIME_OUT(Set.of(Stance.NEUTRAL)),
    ;

    private final Set<Stance> availableStances;

    public boolean isAvailable(Stance stance) {
        return availableStances.contains(stance);
    }

    public boolean isTimeBased() {
        return this == TIME_BASED;
    }

    public boolean isNotTimeBased() {
        return !isTimeBased();
    }
}
