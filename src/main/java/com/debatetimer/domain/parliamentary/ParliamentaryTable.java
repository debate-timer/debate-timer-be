package com.debatetimer.domain.parliamentary;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParliamentaryTable extends DebateTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int duration;

    public ParliamentaryTable(
            Member member,
            String name,
            String agenda,
            int duration,
            boolean warningBell,
            boolean finishBell
    ) {
        super(member, name, agenda, warningBell, finishBell);
        validate(duration);

        this.duration = duration;
    }

    private void validate(int duration) {
        if (duration <= 0) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_TABLE_TIME);
        }
    }

    public void update(ParliamentaryTable renewTable) {
        validate(renewTable.getDuration());

        updateTable(renewTable);
        this.duration = renewTable.getDuration();
    }
}
