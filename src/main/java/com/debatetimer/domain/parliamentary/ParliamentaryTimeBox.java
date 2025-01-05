package com.debatetimer.domain.parliamentary;

import com.debatetimer.domain.BoxType;
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
public class ParliamentaryTimeBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private ParliamentaryTable parliamentaryTable;

    @NotNull
    private int sequence;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Stance stance;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BoxType type;

    @NotNull
    private int time;

    private Integer speaker;

    public ParliamentaryTimeBox(ParliamentaryTable parliamentaryTable, int sequence, Stance stance, BoxType type,
                                int time, Integer speaker) {
        validate(sequence, time, stance, type);
        this.parliamentaryTable = parliamentaryTable;
        this.sequence = sequence;
        this.stance = stance;
        this.type = type;
        this.time = time;
        this.speaker = speaker;
    }

    private void validate(int sequence, int time, Stance stance, BoxType boxType) {
        if (sequence <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_SEQUENCE);
        }
        if (time <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_TIME);
        }

        if (!boxType.isAvailable(stance)) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TIME_BOX_STANCE);
        }
    }
}
