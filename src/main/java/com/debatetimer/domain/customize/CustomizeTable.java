package com.debatetimer.domain.customize;

import com.debatetimer.domain.DebateTable;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.TableType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomizeTable extends DebateTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String prosTeamName;

    @NotNull
    private String consTeamName;

    public CustomizeTable(
            Member member,
            String name,
            String agenda,
            int duration,
            boolean warningBell,
            boolean finishBell,
            String prosTeamName,
            String consTeamName
    ) {
        super(member, name, agenda, duration, warningBell, finishBell);
        this.prosTeamName = prosTeamName;
        this.consTeamName = consTeamName;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TableType getType() {
        return TableType.CUSTOMIZE;
    }
}
