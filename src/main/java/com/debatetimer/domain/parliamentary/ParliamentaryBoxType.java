package com.debatetimer.domain.parliamentary;

import com.debatetimer.domain.Stance;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ParliamentaryBoxType {

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
