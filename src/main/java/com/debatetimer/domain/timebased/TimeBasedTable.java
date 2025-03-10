package com.debatetimer.domain.timebased;

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
public class TimeBasedTable extends DebateTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public TimeBasedTable(
            Member member,
            String name,
            String agenda,
            boolean warningBell,
            boolean finishBell
    ) {
        super(member, name, agenda, warningBell, finishBell);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TableType getType() {
        return TableType.TIME_BASED;
    }

    public void update(TimeBasedTable renewTable) {
        updateTable(renewTable);
    }
}
