package com.debatetimer.domain;

import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BoxType {

    OPENING(Set.of(Stance.PROS, Stance.CONS)),
    REBUTTAL(Set.of(Stance.PROS, Stance.CONS)),
    CROSS(Set.of(Stance.PROS, Stance.CONS)),
    CLOSING(Set.of(Stance.PROS, Stance.CONS)),
    TIME_OUT(Set.of(Stance.NEUTRAL)),
    ;

    private final Set<Stance> availableStances;

    public boolean isAvailable(Stance stance) {
        return availableStances.contains(stance);
    }
}
