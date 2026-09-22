package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-23T10:00:00.123Z");

    private SharingService sharingService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        sharingService = new SharingService(new SharingRoomRegistry(clock), clock);
    }

    @Nested
    class Share {

        @Test
        void 중계_응답에_서버_시각을_담는다() {
            SharingRequest request = new SharingRequest(
                    TimerEventType.PLAY,
                    1L,
                    new TimerEventInfoRequest(CustomizeBoxType.NORMAL, null, 0, 30, true, null, null)
            );

            SharingResponse response = sharingService.share(1L, request);

            assertThat(response.serverTime()).isEqualTo(NOW.toEpochMilli());
        }

        @Test
        void 데이터가_없는_종료_이벤트에도_서버_시각을_담는다() {
            SharingRequest request = new SharingRequest(TimerEventType.FINISHED, 1L, null);

            SharingResponse response = sharingService.share(1L, request);

            assertThat(response.serverTime()).isEqualTo(NOW.toEpochMilli());
        }
    }
}
