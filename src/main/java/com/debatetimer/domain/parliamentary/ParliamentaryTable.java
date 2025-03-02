package com.debatetimer.domain.parliamentary;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
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

    public ParliamentaryTable(
            Member member,
            String name,
            String agenda,
            int duration,
            boolean warningBell,
            boolean finishBell
    ) {
        super(member, name, agenda, duration, warningBell, finishBell);
    }

    @Override
    public TableType getType() {
        return TableType.PARLIAMENTARY;
    }

    public void update(ParliamentaryTable renewTable) {
        updateTable(renewTable);
    }
}
