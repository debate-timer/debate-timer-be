package com.debatetimer.service.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

class SharingServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-23T10:00:00.123Z");
    private static final long ROOM_ID = 1L;

    private SharingRoomRegistry sharingRoomRegistry;
    private SimpMessageSendingOperations messagingTemplate;
    private SharingService sharingService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        sharingRoomRegistry = new SharingRoomRegistry(clock);
        messagingTemplate = mock(SimpMessageSendingOperations.class);
        sharingService = new SharingService(sharingRoomRegistry, messagingTemplate, clock);
    }

    private List<SharingResponse> sentResponses(int expectedCount) {
        ArgumentCaptor<SharingResponse> captor = ArgumentCaptor.forClass(SharingResponse.class);
        verify(messagingTemplate, times(expectedCount)).convertAndSend(eq("/room/" + ROOM_ID), captor.capture());
        return captor.getAllValues();
    }

    private SharingRequest normalEvent(TimerEventType eventType, long version) {
        return new SharingRequest(
                eventType,
                version,
                new TimerEventInfoRequest(CustomizeBoxType.NORMAL, null, 0, 30, true, null, null)
        );
    }

    @Nested
    class Share {

        @Test
        void 룸_채널로_서버_시각을_담은_응답을_보낸다() {
            sharingService.share(ROOM_ID, normalEvent(TimerEventType.PLAY, 1L));

            SharingResponse response = sentResponses(1).get(0);
            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.PLAY),
                    () -> assertThat(response.version()).isEqualTo(1L),
                    () -> assertThat(response.serverTime()).isEqualTo(NOW.toEpochMilli())
            );
        }

        @Test
        void 데이터가_없는_종료_이벤트에도_서버_시각을_담는다() {
            sharingService.share(ROOM_ID, new SharingRequest(TimerEventType.FINISHED, 1L, null));

            assertThat(sentResponses(1).get(0).serverTime()).isEqualTo(NOW.toEpochMilli());
        }

        @Test
        void 이미_공유된_버전_이하의_이벤트는_보내지_않고_룸_상태도_바꾸지_않는다() {
            sharingService.share(ROOM_ID, new SharingRequest(TimerEventType.FINISHED, 200L, null));
            sharingRoomRegistry.reopen(ROOM_ID);

            sharingService.share(ROOM_ID, new SharingRequest(TimerEventType.FINISHED, 100L, null));

            assertAll(
                    () -> assertThat(sentResponses(1)).extracting(SharingResponse::version).containsExactly(200L),
                    () -> assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isFalse()
            );
        }

        @Test
        void 형식이_잘못된_이벤트는_보내지_않고_버전도_소비하지_않는다() {
            SharingRequest invalid = new SharingRequest(TimerEventType.NEXT, 200L, null);
            assertThatThrownBy(() -> sharingService.share(ROOM_ID, invalid))
                    .isInstanceOf(DTClientErrorException.class);
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));

            sharingService.share(ROOM_ID, new SharingRequest(TimerEventType.FINISHED, 200L, null));

            assertThat(sentResponses(1)).extracting(SharingResponse::version).containsExactly(200L);
        }
    }
}
