package com.debatetimer.domain.customize;

import com.debatetimer.domain.member.Member;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomizeTable {

    private final Member member;
    private final TableName name;
    private final String agenda;
    private final TeamName prosTeamName;
    private final TeamName consTeamName;
    private final boolean warningBell;
    private final boolean finishBell;

    public CustomizeTableEntity toEntity() {
        return new CustomizeTableEntity(
                member,
                name.getValue(),
                agenda,
                prosTeamName.getValue(),
                consTeamName.getValue(),
                warningBell,
                finishBell,
                LocalDateTime.now()
        );
    }
}
