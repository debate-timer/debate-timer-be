package com.debatetimer.client.notifier;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.event.sharing.SharingFinishedEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SharingFinishedMessageTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 10, 7, 10, 0, 0);

    private SharingFinishedEvent event(long sharingLogId, long durationSeconds) {
        return new SharingFinishedEvent(
                sharingLogId, 2L, "chairman@email.com", 3L, "토론 테이블",
                STARTED_AT, STARTED_AT.plusSeconds(durationSeconds), durationSeconds, 15);
    }

    @Test
    void 공유_기록_id를_누적_공유_횟수로_강조한다() {
        String message = SharingFinishedMessage.from(event(1234L, 60));

        assertThat(message).startsWith("## 🎉 누적 **1,234**번째 라이브 공유 종료!");
    }

    @Test
    void 사회자_테이블_공유_시간_청중_수를_담는다() {
        String message = SharingFinishedMessage.from(event(1L, 52 * 60 + 7));

        assertThat(message).contains(
                "`2` · chairman@email.com",
                "`3` · 토론 테이블",
                "**52분 7초**",
                "2026-10-07 10:00:00 → 10:52:07",
                "**15명**"
        );
    }
}
