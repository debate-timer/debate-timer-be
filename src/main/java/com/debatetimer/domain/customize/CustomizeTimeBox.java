package com.debatetimer.domain.customize;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import org.springframework.lang.Nullable;

public abstract class CustomizeTimeBox {

    public static final int SPEECH_TYPE_MAX_LENGTH = 10;
    public static final int SPEAKER_MAX_LENGTH = 5;

    private final Stance stance;

    private final String speechType;

    @Nullable
    private final String speaker;

    protected CustomizeTimeBox(Stance stance, String speechType, @Nullable String speaker) {
        validateStance(stance);
        validateSpeechType(speechType);
        validateSpeaker(speaker);

        this.stance = stance;
        this.speechType = speechType;
        this.speaker = speaker;
    }

    private void validateStance(Stance stance) {
        if (stance == null || !isValidStance(stance)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_STANCE);
        }
    }

    protected abstract boolean isValidStance(Stance stance);

    private void validateSpeechType(String speechType) {
        if (speechType == null || speechType.isBlank() || speechType.length() > SPEECH_TYPE_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEECH_TYPE_LENGTH);
        }
    }

    private void validateSpeaker(String speaker) {
        if (speaker != null && speaker.length() > SPEAKER_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEAKER_LENGTH);
        }
    }

    public final Stance getStance() {
        return stance;
    }

    public final String getSpeechType() {
        return speechType;
    }

    @Nullable
    public final String getSpeaker() {
        return speaker;
    }

    public abstract CustomizeBoxType getBoxType();

    @Nullable
    public abstract Integer getTime();

    @Nullable
    public abstract Integer getTimePerTeam();

    @Nullable
    public abstract Integer getTimePerSpeaking();

    public abstract List<Bell> getBells();
}
