package com.debatetimer.client.notifier;

import com.debatetimer.event.sharing.SharingFinishedEvent;
import java.time.format.DateTimeFormatter;

final class SharingFinishedMessage {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 공유 기록 id는 공유를 시작할 때마다 하나씩 늘어나므로 누적 공유 횟수로 보여준다
    private static final String TEMPLATE = """
            ## 🎉 누적 **%,d**번째 라이브 공유 종료!
            > 👤 **사회자**  `%d` · %s
            > 📋 **테이블**  `%d` · %s
            > ⏱️ **공유 시간**  **%d분 %d초**
            > 🕐 **진행**  %s → %s
            > 👥 **참여 청중**  **%d명**""";

    private SharingFinishedMessage() {
    }

    static String from(SharingFinishedEvent event) {
        return TEMPLATE.formatted(
                event.sharingLogId(),
                event.memberId(),
                event.memberEmail(),
                event.tableId(),
                event.tableName(),
                event.durationSeconds() / 60,
                event.durationSeconds() % 60,
                event.startedAt().format(DATE_TIME_FORMATTER),
                event.endedAt().format(TIME_FORMATTER),
                event.audienceCount()
        );
    }
}
