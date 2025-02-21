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

    @NotNull
    private int time;

    public ParliamentaryTimeBox(
            ParliamentaryTable parliamentaryTable,
            int sequence,
            Stance stance,
            ParliamentaryBoxType type,
            int time,
            Integer speaker
    ) {
        super(sequence, stance, speaker);
        validate(time, stance, type);

        this.parliamentaryTable = parliamentaryTable;
        this.type = type;
        this.time = time;
    }

    private void validate(int time, Stance stance, ParliamentaryBoxType boxType) {
        if (time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }
        if (!boxType.isAvailable(stance)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_STANCE);
        }
    }
}
