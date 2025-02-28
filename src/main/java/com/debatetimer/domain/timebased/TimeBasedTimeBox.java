package com.debatetimer.domain.timebased;

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
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeBasedTimeBox extends DebateTimeBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TimeBasedTable timeBasedTable;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TimeBasedBoxType type;

    private Integer timePerTeam;
    private Integer timePerSpeaking;

    public TimeBasedTimeBox(
            TimeBasedTable timeBasedTable,
            int sequence,
            Stance stance,
            TimeBasedBoxType type,
            int time,
            Integer speaker
    ) {
        super(sequence, stance, time, speaker);
        validateStance(stance, type);
        validateNotTimeBasedType(type);

        this.timeBasedTable = timeBasedTable;
        this.type = type;
    }

    public TimeBasedTimeBox(
            TimeBasedTable timeBasedTable,
            int sequence,
            Stance stance,
            TimeBasedBoxType type,
            int time,
            int timePerTeam,
            int timePerSpeaking,
            Integer speaker
    ) {
        super(sequence, stance, time, speaker);
        validateTime(timePerTeam, timePerSpeaking);
        validateTimeBasedTime(time, timePerTeam);
        validateStance(stance, type);
        validateTimeBasedType(type);

        this.timeBasedTable = timeBasedTable;
        this.type = type;
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

    private void validateStance(Stance stance, TimeBasedBoxType boxType) {
        if (!boxType.isAvailable(stance)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_STANCE);
        }
    }

    private void validateTimeBasedType(TimeBasedBoxType boxType) {
        if (boxType.isNotTimeBased()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_FORMAT);
        }
    }

    private void validateNotTimeBasedType(TimeBasedBoxType boxType) {
        if (boxType.isTimeBased()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_FORMAT);
        }
    }
}
