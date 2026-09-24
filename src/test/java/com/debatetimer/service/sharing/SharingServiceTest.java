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
import com.debatetimer.domain.sharing.ChairmanNoticeType;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
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

    private static final String CHAIRMAN = "tab-a";
    private static final String SIMP_SESSION = "simp-1";

    private SharingRoomRegistry sharingRoomRegistry;
    private ChairmanSessionRegistry chairmanSessionRegistry;
    private SimpMessageSendingOperations messagingTemplate;
    private SharingService sharingService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        sharingRoomRegistry = new SharingRoomRegistry(clock);
        chairmanSessionRegistry = new ChairmanSessionRegistry(clock);
        messagingTemplate = mock(SimpMessageSendingOperations.class);
        sharingService = new SharingService(sharingRoomRegistry, chairmanSessionRegistry, messagingTemplate, clock);
    }

    private List<SharingResponse> sentResponses(int expectedCount) {
        ArgumentCaptor<SharingResponse> captor = ArgumentCaptor.forClass(SharingResponse.class);
        verify(messagingTemplate, times(expectedCount)).convertAndSend(eq("/room/" + ROOM_ID), captor.capture());
        return captor.getAllValues();
    }

    private List<ChairmanSharingRequest> sentChairmanNotices(int expectedCount) {
        ArgumentCaptor<ChairmanSharingRequest> captor = ArgumentCaptor.forClass(ChairmanSharingRequest.class);
        verify(messagingTemplate, times(expectedCount)).convertAndSend(eq("/chairman/" + ROOM_ID), captor.capture());
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

        @BeforeEach
        void startChairman() {
            sharingService.startChairman(ROOM_ID, CHAIRMAN, SIMP_SESSION);
        }

        @Test
        void 룸_채널로_서버_시각을_담은_응답을_보낸다() {
            sharingService.share(ROOM_ID, CHAIRMAN, normalEvent(TimerEventType.PLAY, 1L));

            SharingResponse response = sentResponses(1).get(0);
            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.PLAY),
                    () -> assertThat(response.version()).isEqualTo(1L),
                    () -> assertThat(response.serverTime()).isEqualTo(NOW.toEpochMilli())
            );
        }

        @Test
        void 데이터가_없는_종료_이벤트에도_서버_시각을_담는다() {
            sharingService.share(ROOM_ID, CHAIRMAN, new SharingRequest(TimerEventType.FINISHED, 1L, null));

            assertThat(sentResponses(1).get(0).serverTime()).isEqualTo(NOW.toEpochMilli());
        }

        @Test
        void 이미_공유된_버전_이하의_이벤트는_보내지_않고_룸_상태도_바꾸지_않는다() {
            sharingService.share(ROOM_ID, CHAIRMAN, new SharingRequest(TimerEventType.FINISHED, 200L, null));
            sharingRoomRegistry.reopen(ROOM_ID);

            sharingService.share(ROOM_ID, CHAIRMAN, new SharingRequest(TimerEventType.FINISHED, 100L, null));

            assertAll(
                    () -> assertThat(sentResponses(1)).extracting(SharingResponse::version).containsExactly(200L),
                    () -> assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isFalse()
            );
        }

        @Test
        void 형식이_잘못된_이벤트는_보내지_않고_버전도_소비하지_않는다() {
            SharingRequest invalid = new SharingRequest(TimerEventType.NEXT, 200L, null);
            assertThatThrownBy(() -> sharingService.share(ROOM_ID, CHAIRMAN, invalid))
                    .isInstanceOf(DTClientErrorException.class);
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));

            sharingService.share(ROOM_ID, CHAIRMAN, new SharingRequest(TimerEventType.FINISHED, 200L, null));

            assertThat(sentResponses(1)).extracting(SharingResponse::version).containsExactly(200L);
        }
    }

    @Nested
    class ShareByChairmanSession {

        @Test
        void 다른_사회자가_공유를_시작하면_이전_사회자의_이벤트는_중계하지_않는다() {
            sharingService.startChairman(ROOM_ID, "tab-a", "simp-1");
            sharingService.startChairman(ROOM_ID, "tab-b", "simp-2");

            sharingService.share(ROOM_ID, "tab-a", normalEvent(TimerEventType.PLAY, 1L));

            verify(messagingTemplate, never()).convertAndSend(eq("/room/" + ROOM_ID), any(Object.class));
        }

        @Test
        void 밀려난_사회자가_이벤트를_보내면_현재_활성_사회자를_다시_알린다() {
            sharingService.startChairman(ROOM_ID, "tab-a", "simp-1");
            sharingService.startChairman(ROOM_ID, "tab-b", "simp-2");

            sharingService.share(ROOM_ID, "tab-a", normalEvent(TimerEventType.PLAY, 1L));

            assertThat(sentChairmanNotices(2))
                    .allSatisfy(notice -> assertAll(
                            () -> assertThat(notice.type()).isEqualTo(ChairmanNoticeType.REPLACED),
                            () -> assertThat(notice.activeSessionId()).isEqualTo("tab-b")
                    ));
        }

        @Test
        void 새_사회자가_공유를_시작하면_이전_사회자의_버전_기준을_초기화한다() {
            sharingService.startChairman(ROOM_ID, "tab-a", "simp-1");
            sharingService.share(ROOM_ID, "tab-a", normalEvent(TimerEventType.PLAY, 200L));
            sharingService.startChairman(ROOM_ID, "tab-b", "simp-2");

            sharingService.share(ROOM_ID, "tab-b", normalEvent(TimerEventType.PLAY, 100L));

            assertThat(sentResponses(2)).extracting(SharingResponse::version).containsExactly(200L, 100L);
        }

        @Test
        void 공유를_시작하지_않은_세션의_이벤트는_중계하지_않고_활성_사회자로_등록하지도_않는다() {
            sharingService.share(ROOM_ID, "tab-a", normalEvent(TimerEventType.PLAY, 1L));

            assertAll(
                    () -> verify(messagingTemplate, never()).convertAndSend(eq("/room/" + ROOM_ID), any(Object.class)),
                    () -> assertThat(chairmanSessionRegistry.hasActiveChairman(ROOM_ID)).isFalse()
            );
        }

        @Test
        void 다른_사회자가_공유_중일_때_등록되지_않은_세션이_이벤트를_보내면_권한을_넘겨받지_못한다() {
            sharingService.startChairman(ROOM_ID, "tab-a", "simp-1");

            sharingService.share(ROOM_ID, "tab-b", normalEvent(TimerEventType.PLAY, 1L));

            assertAll(
                    () -> verify(messagingTemplate, never()).convertAndSend(eq("/room/" + ROOM_ID), any(Object.class)),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-a")).isTrue()
            );
        }
    }

    @Nested
    class StartChairman {

        @Test
        void 사회자가_공유를_시작하면_종료된_룸을_다시_진행_상태로_되돌린다() {
            sharingRoomRegistry.markFinished(ROOM_ID);

            sharingService.startChairman(ROOM_ID, CHAIRMAN, SIMP_SESSION);

            assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isFalse();
        }

        @Test
        void 밀려난_사회자가_다시_공유를_시작해도_종료된_룸을_되돌리지_않는다() {
            sharingService.startChairman(ROOM_ID, "tab-a", "simp-1");
            sharingService.startChairman(ROOM_ID, "tab-b", "simp-2");
            sharingRoomRegistry.markFinished(ROOM_ID);

            sharingService.startChairman(ROOM_ID, "tab-a", "simp-3");

            assertAll(
                    () -> assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isTrue(),
                    () -> assertThat(chairmanSessionRegistry.isActive(ROOM_ID, "tab-b")).isTrue()
            );
        }

        @Test
        void 다른_사회자가_공유를_시작하면_사회자_채널로_새_활성_사회자를_알린다() {
            sharingService.startChairman(ROOM_ID, "tab-a", "simp-1");

            sharingService.startChairman(ROOM_ID, "tab-b", "simp-2");

            ChairmanSharingRequest notice = sentChairmanNotices(1).get(0);
            assertAll(
                    () -> assertThat(notice.type()).isEqualTo(ChairmanNoticeType.REPLACED),
                    () -> assertThat(notice.activeSessionId()).isEqualTo("tab-b")
            );
        }

        @Test
        void 같은_사회자가_다시_공유를_시작하면_알리지_않는다() {
            sharingService.startChairman(ROOM_ID, CHAIRMAN, "simp-1");

            sharingService.startChairman(ROOM_ID, CHAIRMAN, "simp-2");

            verify(messagingTemplate, never()).convertAndSend(eq("/chairman/" + ROOM_ID), any(Object.class));
        }
    }

    @Nested
    class JoinAudience {

        @Test
        void 종료된_룸이면_청중에게_토론_종료를_알린다() {
            sharingService.startChairman(ROOM_ID, CHAIRMAN, SIMP_SESSION);
            sharingRoomRegistry.markFinished(ROOM_ID);

            sharingService.joinAudience(ROOM_ID);

            assertThat(sentResponses(1).get(0).eventType()).isEqualTo(TimerEventType.FINISHED);
        }

        @Test
        void 활성_사회자가_없으면_청중에게_사회자_부재를_알린다() {
            sharingService.joinAudience(ROOM_ID);

            SharingResponse response = sentResponses(1).get(0);
            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.CHAIRMAN_ABSENT),
                    () -> assertThat(response.version()).isNull(),
                    () -> assertThat(response.data()).isNull()
            );
        }

        @Test
        void 사회자의_연결이_끊겼으면_청중에게_사회자_부재를_알린다() {
            sharingService.startChairman(ROOM_ID, CHAIRMAN, SIMP_SESSION);
            sharingService.leave(SIMP_SESSION);

            sharingService.joinAudience(ROOM_ID);

            assertThat(sentResponses(1).get(0).eventType()).isEqualTo(TimerEventType.CHAIRMAN_ABSENT);
        }

        @Test
        void 활성_사회자가_있으면_사회자에게_상태_공유를_요청한다() {
            sharingService.startChairman(ROOM_ID, CHAIRMAN, SIMP_SESSION);

            sharingService.joinAudience(ROOM_ID);

            ChairmanSharingRequest notice = sentChairmanNotices(1).get(0);
            assertAll(
                    () -> assertThat(notice.type()).isEqualTo(ChairmanNoticeType.SYNC_REQUEST),
                    () -> assertThat(notice.roomId()).isEqualTo(ROOM_ID),
                    () -> verify(messagingTemplate, never()).convertAndSend(eq("/room/" + ROOM_ID), any(Object.class))
            );
        }
    }
}
