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
        validateTimePerTeam(timePerTeam);
        validateTimePerSpeaking(timePerSpeaking);
        if (timePerSpeaking != null && timePerTeam < timePerSpeaking) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BASED_TIME);
        }
    }

    private void validateTimePerTeam(Integer time) {
        if (time == null || time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
    }

    private void validateTimePerSpeaking(Integer timePerSpeaking) {
        if (timePerSpeaking != null && timePerSpeaking <= 0) {
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
