package com.debatetimer.controller.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseStompTest;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.QueueFrameHandler;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.sharing.ChairmanNoticeType;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.fixture.entity.CustomizeTableEntityGenerator;
import com.debatetimer.service.sharing.ChairmanSessionRegistry;
import com.debatetimer.service.sharing.SharingRoomRegistry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;

class SharingWebSocketControllerTest extends BaseStompTest {

    private static final long ROOM_ID = 1L;

    private final String chairmanSessionId = UUID.randomUUID().toString();

    @Autowired
    private SharingRoomRegistry sharingRoomRegistry;

    @Autowired
    private ChairmanSessionRegistry chairmanSessionRegistry;

    @Autowired
    private CustomizeTableEntityGenerator customizeTableEntityGenerator;

    private Member owner;

    @BeforeEach
    void startChairman() throws InterruptedException {
        owner = memberGenerator.generate("owner@email.com");
        customizeTableEntityGenerator.generate(owner); // ROOM_ID(1)번 테이블
        stompSession.subscribe(
                headerGenerator.generateChairmanSubscribeHeader("/chairman/" + ROOM_ID, owner, chairmanSessionId),
                new QueueFrameHandler<>(ChairmanSharingRequest.class)
        );
        assertThat(awaitActive(chairmanSessionId, 3L)).isTrue();
    }

    @AfterEach
    void reopenRoom() {
        sharingRoomRegistry.reopen(ROOM_ID);
        sharingRoomRegistry.resetVersion(ROOM_ID);
        sharingRoomRegistry.resetSyncRequest(ROOM_ID);
        chairmanSessionRegistry.remove(ROOM_ID);
    }

    @Nested
    class Share {

        @Test
        void 사회자가_발생시킨_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.NEXT,
                    null,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            2,
                            30,
                            null,
                            null,
                            null
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler); //청중의 구독

            stompSession.send(headers, request); //사회자의 이벤트 발생

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(request.eventType()),
                    () -> assertThat(response.version()).isNull(),
                    () -> assertThat(response.data()).isNotNull(),
                    () -> assertThat(response.data().timerType()).isEqualTo(request.data().timerType()),
                    () -> assertThat(response.data().sequence()).isEqualTo(request.data().sequence()),
                    () -> assertThat(response.data().currentTeam()).isEqualTo(request.data().currentTeam()),
                    () -> assertThat(response.data().remainingTime()).isEqualTo(request.data().remainingTime()),
                    () -> assertThat(response.data().isRunning()).isNull(),
                    () -> assertThat(response.data().prosRemainingTime()).isNull(),
                    () -> assertThat(response.data().consRemainingTime()).isNull()
            );
        }

        @Test
        void 사회자가_발생시킨_이벤트의_버전과_재생_여부를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.PLAY,
                    1758500000789L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            2,
                            142,
                            true,
                            null,
                            null
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            long sentAt = System.currentTimeMillis();
            stompSession.send(headers, request);

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.PLAY),
                    () -> assertThat(response.version()).isEqualTo(request.version()),
                    () -> assertThat(response.serverTime()).isBetween(sentAt, System.currentTimeMillis()),
                    () -> assertThat(response.data().isRunning()).isTrue()
            );
        }

        @Test
        void 사회자가_아니면_이벤트를_발행할_수_없다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            SharingRequest request = new SharingRequest(
                    TimerEventType.NEXT,
                    null,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            2,
                            30,
                            null,
                            null,
                            null
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler); //청중의 구독
            stompSession.send("/app/event/" + ROOM_ID, request); //사회자의 이벤트 발생

            assertThatThrownBy(() -> handler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }

        @Test
        void 사회자가_발생시킨_토론_종료_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(TimerEventType.FINISHED, 1758500001000L, null);
            stompSession.subscribe("/room/" + ROOM_ID, handler); //청중의 구독

            stompSession.send(headers, request); //사회자의 이벤트 발생

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(request.eventType()),
                    () -> assertThat(response.version()).isEqualTo(request.version()),
                    () -> assertThat(response.data()).isNull()
            );
        }

        @Test
        void 사회자가_토론을_종료하면_룸이_종료_상태가_된다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, new SharingRequest(TimerEventType.FINISHED, null, null));
            handler.getCompletableFuture().get(3L, TimeUnit.SECONDS);

            assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isTrue();
        }

        @Test
        void 종료된_룸에서_사회자가_다른_이벤트를_발생시키면_룸이_다시_진행_상태가_된다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<ChairmanSharingRequest> chairmanHandler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            MessageFrameHandler<SharingResponse> audienceHandler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.SYNC,
                    1758500000123L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            0,
                            180,
                            false,
                            null,
                            null
                    )
            );
            stompSession.subscribe(headerGenerator.generateChairmanSubscribeHeaderWithoutSession("/chairman/" + ROOM_ID, owner), chairmanHandler);
            stompSession.subscribe("/room/" + ROOM_ID, audienceHandler);
            chairmanHandler.getCompletableFuture().get(3L, TimeUnit.SECONDS); // 청중 구독 처리 완료 대기
            sharingRoomRegistry.markFinished(ROOM_ID);

            stompSession.send(headers, request);

            SharingResponse response = audienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.SYNC),
                    () -> assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isFalse()
            );
        }

        @ParameterizedTest
        @NullSource
        void 타이머_타입은_빈_값일_수_없다(CustomizeBoxType boxType) {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.NEXT,
                    null,
                    new TimerEventInfoRequest(
                            boxType,
                            null,
                            2,
                            30,
                            null,
                            null,
                            null
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler); //청중의 구독

            stompSession.send(headers, request); //사회자의 이벤트 발생

            assertThatThrownBy(() -> handler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }
    }

    @Nested
    class ShareVersion {

        @Test
        void 이미_공유된_버전보다_오래된_이벤트는_청중에게_공유되지_않는다() throws InterruptedException {
            QueueFrameHandler<SharingResponse> handler = new QueueFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, normalEvent(TimerEventType.PLAY, 200L));
            stompSession.send(headers, normalEvent(TimerEventType.STOP, 100L));
            stompSession.send(headers, normalEvent(TimerEventType.NEXT, 300L));

            SharingResponse first = handler.poll(3L);
            SharingResponse second = handler.poll(3L);
            assertAll(
                    () -> assertThat(first.version()).isEqualTo(200L),
                    () -> assertThat(second.version()).isEqualTo(300L),
                    () -> assertThat(handler.poll(1L)).isNull()
            );
        }

        @Test
        void 같은_버전의_이벤트가_다시_오면_중복으로_보고_공유하지_않는다() throws InterruptedException {
            QueueFrameHandler<SharingResponse> handler = new QueueFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, normalEvent(TimerEventType.NEXT, 200L));
            stompSession.send(headers, normalEvent(TimerEventType.NEXT, 200L));

            assertAll(
                    () -> assertThat(handler.poll(3L).version()).isEqualTo(200L),
                    () -> assertThat(handler.poll(2L)).isNull()
            );
        }

        @Test
        void 오래된_종료_이벤트는_룸을_종료_상태로_만들지_않는다() throws InterruptedException {
            QueueFrameHandler<SharingResponse> handler = new QueueFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, normalEvent(TimerEventType.PLAY, 200L));
            stompSession.send(headers, new SharingRequest(TimerEventType.FINISHED, 100L, null));
            stompSession.send(headers, normalEvent(TimerEventType.STOP, 300L));

            assertAll(
                    () -> assertThat(handler.poll(3L).version()).isEqualTo(200L),
                    () -> assertThat(handler.poll(3L).version()).isEqualTo(300L),
                    () -> assertThat(sharingRoomRegistry.isFinished(ROOM_ID)).isFalse()
            );
        }
    }

    @Nested
    class ShareSync {

        @Test
        void 사회자가_발생시킨_일반_타이머_동기화_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.SYNC,
                    1758500000123L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            2,
                            142,
                            true,
                            null,
                            null
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, request);

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.SYNC),
                    () -> assertThat(response.version()).isEqualTo(request.version()),
                    () -> assertThat(response.data().timerType()).isEqualTo(CustomizeBoxType.NORMAL),
                    () -> assertThat(response.data().sequence()).isEqualTo(request.data().sequence()),
                    () -> assertThat(response.data().currentTeam()).isNull(),
                    () -> assertThat(response.data().remainingTime()).isEqualTo(request.data().remainingTime()),
                    () -> assertThat(response.data().isRunning()).isTrue(),
                    () -> assertThat(response.data().prosRemainingTime()).isNull(),
                    () -> assertThat(response.data().consRemainingTime()).isNull()
            );
        }

        @Test
        void 사회자가_발생시킨_자유토론_동기화_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.SYNC,
                    1758500000456L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.TIME_BASED,
                            Stance.CONS,
                            4,
                            20,
                            false,
                            45L,
                            38L
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, request);

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.SYNC),
                    () -> assertThat(response.version()).isEqualTo(request.version()),
                    () -> assertThat(response.data().timerType()).isEqualTo(CustomizeBoxType.TIME_BASED),
                    () -> assertThat(response.data().sequence()).isEqualTo(request.data().sequence()),
                    () -> assertThat(response.data().currentTeam()).isEqualTo(Stance.CONS),
                    () -> assertThat(response.data().remainingTime()).isEqualTo(request.data().remainingTime()),
                    () -> assertThat(response.data().isRunning()).isFalse(),
                    () -> assertThat(response.data().prosRemainingTime()).isEqualTo(45L),
                    () -> assertThat(response.data().consRemainingTime()).isEqualTo(38L)
            );
        }

        @Test
        void 동기화_이벤트는_데이터가_없으면_공유되지_않는다() {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(TimerEventType.SYNC, 1758500000123L, null);
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, request);

            assertThatThrownBy(() -> handler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }

        @Test
        void 동기화_이벤트는_재생_여부가_없으면_공유되지_않는다() {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.SYNC,
                    1758500000123L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            2,
                            142,
                            null,
                            null,
                            null
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, request);

            assertThatThrownBy(() -> handler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }

        @ParameterizedTest
        @CsvSource(value = {"null, 38", "45, null", "null, null"}, nullValues = "null")
        void 자유토론_동기화_이벤트는_양_팀의_남은_시간이_없으면_공유되지_않는다(Long prosRemainingTime, Long consRemainingTime) {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest request = new SharingRequest(
                    TimerEventType.SYNC,
                    1758500000456L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.TIME_BASED,
                            Stance.CONS,
                            4,
                            20,
                            false,
                            prosRemainingTime,
                            consRemainingTime
                    )
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, request);

            assertThatThrownBy(() -> handler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }
    }

    @Nested
    class ChairmanSession {

        @Test
        void 사회자_세션_식별자가_없는_이벤트는_청중에게_공유되지_않는다() {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeaderWithoutSession("/app/event/" + ROOM_ID,
                    member);
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(headers, new SharingRequest(TimerEventType.FINISHED, 100L, null));

            assertThatThrownBy(() -> handler.getCompletableFuture().get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }

        @Test
        void 다른_사회자가_공유를_시작하면_이전_사회자의_이벤트는_청중에게_공유되지_않는다() throws InterruptedException {
            QueueFrameHandler<SharingResponse> handler = new QueueFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            String newChairmanSessionId = UUID.randomUUID().toString();
            StompHeaders oldHeaders = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member,
                    chairmanSessionId);
            StompHeaders newHeaders = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member,
                    newChairmanSessionId);
            stompSession.subscribe(
                    headerGenerator.generateChairmanSubscribeHeader("/chairman/" + ROOM_ID, owner, newChairmanSessionId),
                    new QueueFrameHandler<>(ChairmanSharingRequest.class)
            );
            stompSession.subscribe("/room/" + ROOM_ID, handler);

            stompSession.send(oldHeaders, normalEvent(TimerEventType.PLAY, 300L));
            stompSession.send(newHeaders, normalEvent(TimerEventType.STOP, 200L));

            SharingResponse response = handler.poll(3L);
            assertAll(
                    () -> assertThat(response.version()).isEqualTo(200L),
                    () -> assertThat(handler.poll(1L)).isNull()
            );
        }

        @Test
        void 다른_사회자가_공유를_시작하면_사회자_채널로_새_활성_사회자를_알린다() throws InterruptedException {
            QueueFrameHandler<ChairmanSharingRequest> chairmanHandler = new QueueFrameHandler<>(
                    ChairmanSharingRequest.class);
            String newChairmanSessionId = UUID.randomUUID().toString();
            stompSession.subscribe(headerGenerator.generateChairmanSubscribeHeaderWithoutSession("/chairman/" + ROOM_ID, owner), chairmanHandler);

            stompSession.subscribe(
                    headerGenerator.generateChairmanSubscribeHeader("/chairman/" + ROOM_ID, owner, newChairmanSessionId),
                    new QueueFrameHandler<>(ChairmanSharingRequest.class)
            );

            ChairmanSharingRequest notice = chairmanHandler.poll(3L);
            assertAll(
                    () -> assertThat(notice.type()).isEqualTo(ChairmanNoticeType.REPLACED),
                    () -> assertThat(notice.activeSessionId()).isEqualTo(newChairmanSessionId)
            );
        }

        @Test
        void 밀려난_사회자가_이벤트를_보내면_사회자_채널로_활성_사회자를_다시_알린다() throws InterruptedException {
            QueueFrameHandler<ChairmanSharingRequest> chairmanHandler = new QueueFrameHandler<>(
                    ChairmanSharingRequest.class);
            Member member = memberGenerator.generate("example@email.com");
            String newChairmanSessionId = UUID.randomUUID().toString();
            stompSession.subscribe(headerGenerator.generateChairmanSubscribeHeaderWithoutSession("/chairman/" + ROOM_ID, owner), chairmanHandler);
            stompSession.subscribe(
                    headerGenerator.generateChairmanSubscribeHeader("/chairman/" + ROOM_ID, owner, newChairmanSessionId),
                    new QueueFrameHandler<>(ChairmanSharingRequest.class)
            );
            assertThat(chairmanHandler.poll(3L)).isNotNull(); // 교체 알림 대기

            stompSession.send(
                    headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId),
                    normalEvent(TimerEventType.PLAY, 300L)
            );

            ChairmanSharingRequest notice = chairmanHandler.poll(3L);
            assertAll(
                    () -> assertThat(notice.type()).isEqualTo(ChairmanNoticeType.REPLACED),
                    () -> assertThat(notice.activeSessionId()).isEqualTo(newChairmanSessionId)
            );
        }
    }

    private SharingRequest normalEvent(TimerEventType eventType, long version) {
        return new SharingRequest(
                eventType,
                version,
                new TimerEventInfoRequest(CustomizeBoxType.NORMAL, null, 0, 30, null, null, null)
        );
    }

    private boolean awaitActive(String sessionId, long timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        while (System.currentTimeMillis() < deadline) {
            if (chairmanSessionRegistry.isActive(ROOM_ID, sessionId)) {
                return true;
            }
            Thread.sleep(50L);
        }
        return false;
    }
}
