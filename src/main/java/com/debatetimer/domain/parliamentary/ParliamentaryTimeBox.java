package com.debatetimer.domain.parliamentary;

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
public class ParliamentaryTimeBox extends DebateTimeBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private ParliamentaryTable parliamentaryTable;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ParliamentaryBoxType type;

    public ParliamentaryTimeBox(
            ParliamentaryTable parliamentaryTable,
            int sequence,
            Stance stance,
            ParliamentaryBoxType type,
            int time,
            Integer speaker
    ) {
        super(sequence, stance, time, convertToSpeaker(speaker));
        validate(stance, type);
        validateSpeakerNumber(speaker);

        this.parliamentaryTable = parliamentaryTable;
        this.type = type;
    }

    private static String convertToSpeaker(Integer speakerNumber) {
        if (speakerNumber == null) {
            return null;
        }
        return String.valueOf(speakerNumber);
    }

    private void validate(Stance stance, ParliamentaryBoxType boxType) {
        if (!boxType.isAvailable(stance)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_STANCE);
        }
    }

    private void validateSpeakerNumber(Integer speaker) {
        if (speaker != null && speaker <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SPEAKER);
        }
    }

    public Integer getSpeakerNumber() {
        String speaker = getSpeaker();
        if (speaker == null || speaker.isBlank() || speaker.equals("null")) {
            return null;
        }
        return Integer.parseInt(speaker);
    }
}
