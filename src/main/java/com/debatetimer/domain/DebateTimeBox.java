package com.debatetimer.domain;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DebateTimeBox {

    public static final int SPEAKER_MAX_LENGTH = 5;

    private int sequence;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Stance stance;

    private int time;
    private String speaker;

    protected DebateTimeBox(int sequence, Stance stance, int time, String speaker) {
        validateSpeaker(speaker);
        validateSequence(sequence);
        validateTime(time);

        this.sequence = sequence;
        this.stance = stance;
        this.time = time;
        this.speaker = speaker;
    }

    private void validateSpeaker(String speaker) {
        if (speaker != null && (speaker.isBlank() || speaker.length() > SPEAKER_MAX_LENGTH)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEAKER_LENGTH);
        }
    }

    private void validateSequence(int sequence) {
        if (sequence <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE);
        }
    }

    private void validateTime(int time) {
        if (time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
    }
}
