package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import org.springframework.lang.Nullable;

public final class NormalTimeBox extends CustomizeTimeBox {

    private final int time;
    private final List<Bell> bells;

    public NormalTimeBox(Stance stance, String speechType, @Nullable String speaker, Integer time, List<Bell> bells) {
        super(stance, speechType, speaker);
        validateTime(time);
        this.time = time;
        this.bells = bells;
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

    @Override
    public List<Bell> getBells() {
        return bells;
    }
}
