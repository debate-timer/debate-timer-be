package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.springframework.lang.Nullable;

public final class NormalTimeBoxDomain extends CustomizeTimeBoxDomain {

    private final int time;

    public NormalTimeBoxDomain(Stance stance, String speechType, @Nullable String speaker, Integer time) {
        super(stance, speechType, speaker);

        validateTime(time);
        this.time = time;
    }

    private void validateTime(Integer time) {
        if (time == null || time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
    }

    @Override
    protected boolean isValidStance(Stance stance) {
        return true;
    }

    @Override
    public CustomizeBoxType getBoxType() {
        return CustomizeBoxType.NORMAL;
    }

    @Override
    public Integer getTime() {
        return time;
    }

    @Nullable
    @Override
    public Integer getTimePerTeam() {
        return null;
    }

    @Nullable
    @Override
    public Integer getTimePerSpeaking() {
        return null;
    }
}
