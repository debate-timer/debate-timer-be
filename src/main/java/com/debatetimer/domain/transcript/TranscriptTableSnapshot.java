package com.debatetimer.domain.transcript;

import com.debatetimer.domain.customize.CustomizeTable;
import org.springframework.lang.Nullable;

/**
 * 속기록을 만들 때의 시간표 이름·주제·팀 이름. 이후 시간표를 수정하거나 지워도 바뀌지 않는다.
 * 스냅샷 도입 전에 지워진 시간표의 기록은 값이 비어 있을 수 있다.
 */
public record TranscriptTableSnapshot(
        @Nullable String tableName,
        @Nullable String agenda,
        @Nullable String prosTeamName,
        @Nullable String consTeamName
) {

    public static TranscriptTableSnapshot from(CustomizeTable table) {
        return new TranscriptTableSnapshot(table.getName(), table.getAgenda(), table.getProsTeamName(),
                table.getConsTeamName());
    }
}
