package com.debatetimer.domain.customize;

import com.debatetimer.domain.Stance;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomizeTimeBox {

    public static final int SPEECH_TYPE_MAX_LENGTH = 10;
    public static final int TIME_MULTIPLIER = 2;
    public static final int SPEAKER_MAX_LENGTH = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private CustomizeTable customizeTable;

    private int sequence;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Stance stance;

    private int time;
    private String speaker;

    @NotBlank
    private String speechType;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private CustomizeBoxType boxType;

    private Integer timePerTeam;
    private Integer timePerSpeaking;

    public CustomizeTimeBox(
            CustomizeTable customizeTable,
            int sequence,
            Stance stance,
            String speechType,
            CustomizeBoxType boxType,
            Integer time,
            String speaker
    ) {
        validateNotTimeBasedType(boxType);
        validateSpeechType(speechType);
        validateSpeaker(speaker);
        validateSequence(sequence);
        validateTime(time);

        this.customizeTable = customizeTable;
        this.sequence = sequence;
        this.stance = stance;
        this.time = time;
        this.speaker = initializeSpeaker(speaker);
        this.speechType = speechType;
        this.boxType = boxType;
    }

    public CustomizeTimeBox(
            CustomizeTable customizeTable,
            int sequence,
            Stance stance,
            String speechType,
            CustomizeBoxType boxType,
            Integer timePerTeam,
            Integer timePerSpeaking,
            String speaker
    ) {
        validateTimeBasedTimes(timePerTeam, timePerSpeaking);
        validateTimeBasedType(boxType);
        validateSpeechType(speechType);
        validateSpeaker(speaker);
        validateSequence(sequence);
        validateTime(convertToTime(timePerTeam));

        this.sequence = sequence;
        this.stance = stance;
        this.time = convertToTime(timePerTeam);
        this.speaker = initializeSpeaker(speaker);
        this.customizeTable = customizeTable;
        this.speechType = speechType;
        this.boxType = boxType;
        this.timePerTeam = timePerTeam;
        this.timePerSpeaking = timePerSpeaking;
    }

    private static int convertToTime(Integer timePerTeam) {
        if (timePerTeam == null) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_FORMAT);
        }
        return timePerTeam * TIME_MULTIPLIER;
    }


    private String initializeSpeaker(String speaker) {
        if (speaker == null || speaker.isBlank()) {
            return null;
        }
        return speaker;
    }

    private void validateSpeaker(String speaker) {
        if (speaker != null && speaker.length() > SPEAKER_MAX_LENGTH) {
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

    private void validateTime(Integer time) {
        if (time == null || time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
    }

    private void validateTimeBasedTimes(Integer timePerTeam, Integer timePerSpeaking) {
        validateTime(timePerTeam);
        if (timePerSpeaking == null) {
            return;
        }

        validateTime(timePerSpeaking);
        if (timePerTeam < timePerSpeaking) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BASED_TIME);
        }
    }

    private void validateTimeBasedType(CustomizeBoxType boxType) {
        if (boxType.isNotTimeBased()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_FORMAT);
        }
    }

    private void validateNotTimeBasedType(CustomizeBoxType boxType) {
        if (boxType.isTimeBased()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_FORMAT);
        }
    }

    private void validateSpeechType(String speechType) {
        if (speechType.length() > SPEECH_TYPE_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEECH_TYPE_LENGTH);
        }
    }
}
