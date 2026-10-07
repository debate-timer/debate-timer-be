package com.debatetimer.event.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseStompTest;
import com.debatetimer.controller.sharing.SharingWebSocketController;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.QueueFrameHandler;
import com.debatetimer.domain.customize.CustomizeBoxType;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

class RoomSubscribeInterceptorTest extends BaseStompTest {

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
    void setUpRoomOwner() {
        owner = memberGenerator.generate("owner@email.com");
        customizeTableEntityGenerator.generate(owner); // ROOM_ID(1)번 테이블
    }

    @AfterEach
    void reopenRoom() {
        sharingRoomRegistry.reopen(ROOM_ID);
        sharingRoomRegistry.resetVersion(ROOM_ID);
        sharingRoomRegistry.resetSyncRequest(ROOM_ID);
        chairmanSessionRegistry.remove(ROOM_ID);
    }

    @Nested
    class AfterMessageHandled {

        @Test
        void 새로운_청중이_공유되면_사회자에게_정보공유_트리거를_발송한다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<ChairmanSharingRequest> handler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            subscribeChairman(handler);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            ChairmanSharingRequest sharingRequest = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(sharingRequest.type()).isEqualTo(ChairmanNoticeType.SYNC_REQUEST),
                    () -> assertThat(sharingRequest.roomId()).isEqualTo(ROOM_ID)
            );
        }

        @Test
        void 사회자가_있는_진행_중인_룸에는_새로운_청중이_구독해도_서버가_이벤트를_보내지_않는다() {
            subscribeChairman(new QueueFrameHandler<>(ChairmanSharingRequest.class));
            MessageFrameHandler<SharingResponse> audienceHandler = new MessageFrameHandler<>(SharingResponse.class);

            stompSession.subscribe("/room/" + ROOM_ID, audienceHandler);

            assertThatThrownBy(() -> audienceHandler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }

        @Test
        void 종료된_룸에_청중이_구독하면_토론_종료_이벤트를_보낸다() throws ExecutionException, InterruptedException, TimeoutException {
            sharingRoomRegistry.markFinished(ROOM_ID);
            MessageFrameHandler<SharingResponse> audienceHandler = new MessageFrameHandler<>(SharingResponse.class);

            stompSession.subscribe("/room/" + ROOM_ID, audienceHandler);

            SharingResponse response = audienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.FINISHED),
                    () -> assertThat(response.version()).isNull(),
                    () -> assertThat(response.data()).isNull()
            );
        }

        @Test
        void 종료된_룸에_청중이_구독하면_사회자에게_정보공유_트리거를_발송하지_않는다() throws InterruptedException {
            QueueFrameHandler<ChairmanSharingRequest> chairmanHandler = new QueueFrameHandler<>(
                    ChairmanSharingRequest.class);
            subscribeChairman(chairmanHandler);
            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));
            assertThat(chairmanHandler.poll(3L)).isNotNull(); // 사회자 구독 처리 완료 대기
            sharingRoomRegistry.markFinished(ROOM_ID);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            assertThat(chairmanHandler.poll(2L)).isNull();
        }

        @Test
        void 사회자가_구독하면_종료된_룸을_다시_진행_상태로_되돌린다() throws InterruptedException {
            sharingRoomRegistry.markFinished(ROOM_ID);

            subscribeChairman(new MessageFrameHandler<>(ChairmanSharingRequest.class));

            assertThat(awaitReopened(3L)).isTrue();
        }

        @Test
        void 사회자가_구독하면_룸의_버전_기준을_초기화한다() throws InterruptedException {
            sharingRoomRegistry.acceptVersion(ROOM_ID, 1_000L);

            subscribeChairman(new MessageFrameHandler<>(ChairmanSharingRequest.class));

            assertThat(awaitVersionReset(3L)).isTrue();
        }

        @Test
        void 종료된_룸에_사회자가_다시_구독하면_새로운_청중에_대해_사회자에게_정보공유_트리거를_발송한다() throws ExecutionException, InterruptedException, TimeoutException {
            sharingRoomRegistry.markFinished(ROOM_ID);
            MessageFrameHandler<ChairmanSharingRequest> chairmanHandler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            subscribeChairman(chairmanHandler);
            assertThat(awaitReopened(3L)).isTrue();

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            ChairmanSharingRequest sharingRequest = chairmanHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(sharingRequest.roomId()).isEqualTo(ROOM_ID);
        }

        @Test
        void 사회자가_토론을_종료한_뒤_입장한_청중은_서버로부터_토론_종료_이벤트를_받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> existingAudienceHandler = new MessageFrameHandler<>(
                    SharingResponse.class);
            MessageFrameHandler<SharingResponse> newAudienceHandler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            subscribeChairman(new QueueFrameHandler<>(ChairmanSharingRequest.class));
            stompSession.subscribe("/room/" + ROOM_ID, existingAudienceHandler);
            stompSession.send(headers, new SharingRequest(TimerEventType.FINISHED, 100L, null));
            SharingResponse chairmanFinished = existingAudienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            stompSession.subscribe("/room/" + ROOM_ID, newAudienceHandler);

            SharingResponse serverFinished = newAudienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(chairmanFinished.version()).isEqualTo(100L),
                    () -> assertThat(serverFinished.eventType()).isEqualTo(TimerEventType.FINISHED),
                    () -> assertThat(serverFinished.version()).isNull(),
                    () -> assertThat(serverFinished.data()).isNull()
            );
        }

        @Test
        void 종료된_룸을_사회자가_다시_진행하면_새로운_청중에_대해_사회자에게_정보공유_트리거를_발송한다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<ChairmanSharingRequest> firstChairmanHandler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            MessageFrameHandler<ChairmanSharingRequest> secondChairmanHandler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            MessageFrameHandler<SharingResponse> existingAudienceHandler = new MessageFrameHandler<>(
                    SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member, chairmanSessionId);
            SharingRequest playRequest = new SharingRequest(
                    TimerEventType.PLAY,
                    200L,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            null,
                            0,
                            180,
                            true,
                            null,
                            null
                    )
            );
            subscribeChairman(firstChairmanHandler);
            stompSession.subscribe("/room/" + ROOM_ID, existingAudienceHandler);
            firstChairmanHandler.getCompletableFuture().get(3L, TimeUnit.SECONDS); // 기존 청중 구독 처리 완료 대기
            sharingRoomRegistry.markFinished(ROOM_ID);
            stompSession.send(headers, playRequest);
            existingAudienceHandler.getCompletableFuture().get(3L, TimeUnit.SECONDS); // 사회자 이벤트 처리 완료 대기
            subscribeChairman(secondChairmanHandler);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            ChairmanSharingRequest sharingRequest = secondChairmanHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(sharingRequest.roomId()).isEqualTo(ROOM_ID);
        }
    }

    @Nested
    class SyncRequestInterval {

        @Test
        void 청중이_연달아_구독해도_사회자에게_정보공유_트리거를_한_번만_발송한다() throws Exception {
            QueueFrameHandler<ChairmanSharingRequest> chairmanHandler = new QueueFrameHandler<>(
                    ChairmanSharingRequest.class);
            subscribeChairman(chairmanHandler);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));
            assertThat(chairmanHandler.poll(3L)).isNotNull(); // 첫 구독 처리 완료 대기
            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            assertThat(chairmanHandler.poll(2L)).isNull();
        }

        @Test
        void 요청_간격_기준을_지우면_사회자에게_정보공유_트리거를_다시_발송한다() throws Exception {
            QueueFrameHandler<ChairmanSharingRequest> chairmanHandler = new QueueFrameHandler<>(
                    ChairmanSharingRequest.class);
            subscribeChairman(chairmanHandler);
            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));
            assertThat(chairmanHandler.poll(3L)).isNotNull(); // 첫 구독 처리 완료 대기

            sharingRoomRegistry.resetSyncRequest(ROOM_ID);
            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            ChairmanSharingRequest sharingRequest = chairmanHandler.poll(3L);
            assertAll(
                    () -> assertThat(sharingRequest).isNotNull(),
                    () -> assertThat(sharingRequest.type()).isEqualTo(ChairmanNoticeType.SYNC_REQUEST)
            );
        }
    }

    @Nested
    class ChairmanPresence {

        @Test
        void 활성_사회자가_없는_룸에_청중이_구독하면_사회자_부재_이벤트를_보낸다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> audienceHandler = new MessageFrameHandler<>(SharingResponse.class);

            stompSession.subscribe("/room/" + ROOM_ID, audienceHandler);

            SharingResponse response = audienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.CHAIRMAN_ABSENT),
                    () -> assertThat(response.version()).isNull(),
                    () -> assertThat(response.data()).isNull()
            );
        }

        @Test
        void 사회자_세션_식별자_없이_구독한_사회자는_활성_사회자로_등록되지_않는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> audienceHandler = new MessageFrameHandler<>(SharingResponse.class);
            stompSession.subscribe(headerGenerator.generateChairmanSubscribeHeaderWithoutSession("/chairman/" + ROOM_ID, owner), new QueueFrameHandler<>(ChairmanSharingRequest.class));

            stompSession.subscribe("/room/" + ROOM_ID, audienceHandler);

            SharingResponse response = audienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(response.eventType()).isEqualTo(TimerEventType.CHAIRMAN_ABSENT);
        }

        @Test
        void 사회자의_연결이_끊긴_뒤_입장한_청중은_사회자_부재_이벤트를_받는다() throws ExecutionException, InterruptedException, TimeoutException {
            subscribeChairman(new QueueFrameHandler<>(ChairmanSharingRequest.class));
            assertThat(awaitActiveChairman(true, 3L)).isTrue();
            stompSession.disconnect();
            assertThat(awaitActiveChairman(false, 3L)).isTrue();
            connect();
            MessageFrameHandler<SharingResponse> audienceHandler = new MessageFrameHandler<>(SharingResponse.class);

            stompSession.subscribe("/room/" + ROOM_ID, audienceHandler);

            SharingResponse response = audienceHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(response.eventType()).isEqualTo(TimerEventType.CHAIRMAN_ABSENT);
        }

        @Test
        void 같은_사회자가_다시_구독하면_버전_기준을_유지한다() throws InterruptedException {
            subscribeChairman(new QueueFrameHandler<>(ChairmanSharingRequest.class));
            assertThat(awaitActiveChairman(true, 3L)).isTrue();
            sharingRoomRegistry.acceptVersion(ROOM_ID, 1_000L);

            subscribeChairman(new QueueFrameHandler<>(ChairmanSharingRequest.class));

            Thread.sleep(500L); // 재구독 처리 완료 대기
            assertThat(sharingRoomRegistry.acceptVersion(ROOM_ID, 1L)).isFalse();
        }
    }

    @Nested
    class ChairmanAuthorization {

        @Test
        void 사회자_토큰_없이_사회자_채널을_구독하면_활성_사회자가_되지_못한다() throws InterruptedException {
            StompHeaders headers = new StompHeaders();
            headers.setDestination("/chairman/" + ROOM_ID);
            headers.add(SharingWebSocketController.CHAIRMAN_SESSION_HEADER, chairmanSessionId);

            stompSession.subscribe(headers, new QueueFrameHandler<>(ChairmanSharingRequest.class));

            assertThat(awaitActiveChairman(true, 2L)).isFalse();
        }

        @Test
        void 테이블_소유자가_아닌_회원은_사회자_채널을_구독해도_활성_사회자가_되지_못한다() throws InterruptedException {
            Member other = memberGenerator.generate("other@email.com");

            stompSession.subscribe(
                    headerGenerator.generateChairmanSubscribeHeader("/chairman/" + ROOM_ID, other, chairmanSessionId),
                    new QueueFrameHandler<>(ChairmanSharingRequest.class)
            );

            assertThat(awaitActiveChairman(true, 2L)).isFalse();
        }

        @Test
        void 권한_없는_구독은_기존_활성_사회자를_밀어내지_못한다() throws Exception {
            subscribeChairman(new QueueFrameHandler<>(ChairmanSharingRequest.class));
            assertThat(awaitActiveChairman(true, 3L)).isTrue();
            StompSession ownerSession = stompSession;
            connect(); // 공격자용 새 연결
            StompHeaders headers = new StompHeaders();
            headers.setDestination("/chairman/" + ROOM_ID);
            headers.add(SharingWebSocketController.CHAIRMAN_SESSION_HEADER, UUID.randomUUID().toString());

            stompSession.subscribe(headers, new QueueFrameHandler<>(ChairmanSharingRequest.class));

            Thread.sleep(1000L);
            assertThat(chairmanSessionRegistry.isActive(ROOM_ID, chairmanSessionId)).isTrue();
            ownerSession.disconnect();
        }
    }

    private void subscribeChairman(StompFrameHandler handler) {
        stompSession.subscribe(
                headerGenerator.generateChairmanSubscribeHeader("/chairman/" + ROOM_ID, owner, chairmanSessionId),
                handler
        );
    }

    private boolean awaitActiveChairman(boolean expected, long timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        while (System.currentTimeMillis() < deadline) {
            if (chairmanSessionRegistry.hasActiveChairman(ROOM_ID) == expected) {
                return true;
            }
            Thread.sleep(50L);
        }
        return false;
    }

    private boolean awaitVersionReset(long timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        while (System.currentTimeMillis() < deadline) {
            if (sharingRoomRegistry.acceptVersion(ROOM_ID, 1L)) {
                return true;
            }
            Thread.sleep(50L);
        }
        return false;
    }

    private boolean awaitReopened(long timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        while (System.currentTimeMillis() < deadline) {
            if (!sharingRoomRegistry.isFinished(ROOM_ID)) {
                return true;
            }
            Thread.sleep(50L);
        }
        return !sharingRoomRegistry.isFinished(ROOM_ID);
    }
}
