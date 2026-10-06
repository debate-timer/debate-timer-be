package com.debatetimer.event.sharing;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.debatetimer.client.notifier.SharingNotifier;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SharingFinishedListenerTest {

    private static final SharingFinishedEvent EVENT = new SharingFinishedEvent(
            1L, 2L, "chairman@email.com", 3L, "테이블",
            LocalDateTime.of(2026, 10, 7, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 30), 1800, 5);

    private SharingNotifier sharingNotifier;
    private SharingFinishedListener listener;

    @BeforeEach
    void setUp() {
        sharingNotifier = mock(SharingNotifier.class);
        listener = new SharingFinishedListener(sharingNotifier);
    }

    @Test
    void 공유가_종료되면_공유_통계를_알린다() {
        listener.handleSharingFinished(EVENT);

        verify(sharingNotifier).sendSharingFinished(EVENT);
    }

    @Test
    void 알림_발송에_실패해도_예외를_던지지_않는다() {
        doThrow(new IllegalStateException("discord down")).when(sharingNotifier).sendSharingFinished(any());

        assertThatCode(() -> listener.handleSharingFinished(EVENT)).doesNotThrowAnyException();
    }
}
