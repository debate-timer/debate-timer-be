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

    private int sequence;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Stance stance;

    private Integer speaker;

    public DebateTimeBox(int sequence, Stance stance, Integer speaker) {
        validateSequence(sequence);
        validateSpeakerNumber(speaker);

        this.sequence = sequence;
        this.stance = stance;
        this.speaker = speaker;
    }

    private void validateSequence(int sequence) {
        if (sequence <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE);
        }
    }

    private void validateSpeakerNumber(Integer speaker) {
        if (speaker != null && speaker <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEAKER);
        }
    }
}
