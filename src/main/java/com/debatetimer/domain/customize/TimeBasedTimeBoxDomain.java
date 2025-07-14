package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import org.springframework.lang.Nullable;

public class TimeBasedTimeBoxDomain extends CustomizeTimeBoxDomain {

    private final int timePerTeam;

    @Nullable
    private final Integer timePerSpeaking;

    public TimeBasedTimeBoxDomain(Stance stance,
                                  String speechType,
                                  @Nullable String speaker,
                                  Integer timePerTeam,
                                  @Nullable Integer timePerSpeaking) {
        super(stance, speechType, speaker);

        validateTimes(timePerTeam, timePerSpeaking);
        this.timePerTeam = timePerTeam;
        this.timePerSpeaking = timePerSpeaking;
    }

    private void validateTimes(Integer timePerTeam, Integer timePerSpeaking) {
        validateTime(timePerTeam);
        if (timePerSpeaking == null) {
            return;
        }

        validateTime(timePerSpeaking);
        if (timePerTeam < timePerSpeaking) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BASED_TIME);
        }
    }

    private void validateTime(Integer time) {
        if (time == null || time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
    }

    @Override
    protected boolean isValidStance(Stance stance) {
        return stance.isNeutralStance();
    }

    @Override
    public CustomizeBoxType getBoxType() {
        return CustomizeBoxType.TIME_BASED;
    }

    @Override
    @Nullable
    public Integer getTime() {
        return null;
    }

    @Override
    public Integer getTimePerTeam() {
        return timePerTeam;
    }

    @Override
    @Nullable
    public Integer getTimePerSpeaking() {
        return timePerSpeaking;
    }
}
