package com.debatetimer.domain.customize;

import com.debatetimer.domain.DebateTimeBox;
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
public class CustomizeTimeBox extends DebateTimeBox {

    public static final int SPEECH_TYPE_MAX_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private CustomizeTable customizeTable;

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
            int time,
            String speaker
    ) {
        super(sequence, stance, time, speaker);
        validateNotTimeBasedType(boxType);
        validateSpeechType(speechType);

        this.customizeTable = customizeTable;
        this.speechType = speechType;
        this.boxType = boxType;
    }

    public CustomizeTimeBox(
            CustomizeTable customizeTable,
            int sequence,
            Stance stance,
            String speechType,
            CustomizeBoxType boxType,
            int time,
            int timePerTeam,
            Integer timePerSpeaking,
            String speaker
    ) {
        super(sequence, stance, time, speaker);
        validateTime(timePerTeam, timePerSpeaking);
        validateTimeBasedTime(time, timePerTeam);
        validateTimeBasedType(boxType);
        validateSpeechType(speechType);

        this.customizeTable = customizeTable;
        this.speechType = speechType;
        this.boxType = boxType;
        this.timePerTeam = timePerTeam;
        this.timePerSpeaking = timePerSpeaking;
    }

    private void validateTime(int time) {
        if (time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
    }

    private void validateTime(int timePerTeam, int timePerSpeaking) {
        validateTime(timePerTeam);
        validateTime(timePerSpeaking);
        if (timePerTeam < timePerSpeaking) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BASED_TIME);
        }
    }

    private void validateTimeBasedTime(int time, int timePerTeam) {
        if (time != timePerTeam * 2) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BASED_TIME_IS_NOT_DOUBLE);
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
        if (speechType.isBlank() || speechType.length() > SPEECH_TYPE_MAX_LENGTH) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEECH_TYPE_LENGTH);
        }
    }
}
