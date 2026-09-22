package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-23T10:00:00.123Z");

    private SharingRoomRegistry sharingRoomRegistry;
    private SharingService sharingService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        sharingRoomRegistry = new SharingRoomRegistry(clock);
        sharingService = new SharingService(sharingRoomRegistry, clock);
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

            SharingResponse response = sharingService.share(1L, request).orElseThrow();

            assertThat(response.serverTime()).isEqualTo(NOW.toEpochMilli());
        }

        @Test
        void 데이터가_없는_종료_이벤트에도_서버_시각을_담는다() {
            SharingRequest request = new SharingRequest(TimerEventType.FINISHED, 1L, null);

            SharingResponse response = sharingService.share(1L, request).orElseThrow();

            assertThat(response.serverTime()).isEqualTo(NOW.toEpochMilli());
        }

        @Test
        void 이미_공유된_버전_이하의_이벤트는_공유하지_않는다() {
            sharingService.share(1L, new SharingRequest(TimerEventType.FINISHED, 200L, null));
            sharingRoomRegistry.reopen(1L);

            Optional<SharingResponse> response = sharingService.share(
                    1L, new SharingRequest(TimerEventType.FINISHED, 100L, null));

            assertAll(
                    () -> assertThat(response).isEmpty(),
                    () -> assertThat(sharingRoomRegistry.isFinished(1L)).isFalse()
            );
        }

        @Test
        void 형식이_잘못된_이벤트는_버전을_소비하지_않는다() {
            SharingRequest invalid = new SharingRequest(TimerEventType.NEXT, 200L, null);
            assertThatThrownBy(() -> sharingService.share(1L, invalid))
                    .isInstanceOf(DTClientErrorException.class);

            Optional<SharingResponse> response = sharingService.share(
                    1L, new SharingRequest(TimerEventType.FINISHED, 200L, null));

            assertThat(response).isPresent();
        }
    }
}
