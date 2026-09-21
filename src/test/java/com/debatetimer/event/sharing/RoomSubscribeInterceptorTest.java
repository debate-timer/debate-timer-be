package com.debatetimer.event.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseStompTest;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.service.sharing.SharingRoomRegistry;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

class RoomSubscribeInterceptorTest extends BaseStompTest {

    private static final long ROOM_ID = 1L;

    @Autowired
    private SharingRoomRegistry sharingRoomRegistry;

    @AfterEach
    void reopenRoom() {
        sharingRoomRegistry.reopen(ROOM_ID);
    }

    @Nested
    class AfterMessageHandled {

        @Test
        void 새로운_청중이_공유되면_사회자에게_정보공유_트리거를_발송한다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<ChairmanSharingRequest> handler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            stompSession.subscribe("/chairman/" + ROOM_ID, handler);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            ChairmanSharingRequest sharingRequest = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(sharingRequest.roomId()).isEqualTo(ROOM_ID);
        }

        @Test
        void 새로운_청중이_구독해도_진행_중인_룸에는_서버가_이벤트를_보내지_않는다() {
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
            stompSession.subscribe("/chairman/" + ROOM_ID, chairmanHandler);
            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));
            assertThat(chairmanHandler.poll(3L)).isNotNull(); // 사회자 구독 처리 완료 대기
            sharingRoomRegistry.markFinished(ROOM_ID);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            assertThat(chairmanHandler.poll(2L)).isNull();
        }

        @Test
        void 사회자가_구독하면_종료된_룸을_다시_진행_상태로_되돌린다() throws InterruptedException {
            sharingRoomRegistry.markFinished(ROOM_ID);

            stompSession.subscribe("/chairman/" + ROOM_ID, new MessageFrameHandler<>(ChairmanSharingRequest.class));

            assertThat(awaitReopened(3L)).isTrue();
        }

        @Test
        void 종료된_룸에_사회자가_다시_구독하면_새로운_청중에_대해_사회자에게_정보공유_트리거를_발송한다() throws ExecutionException, InterruptedException, TimeoutException {
            sharingRoomRegistry.markFinished(ROOM_ID);
            MessageFrameHandler<ChairmanSharingRequest> chairmanHandler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            stompSession.subscribe("/chairman/" + ROOM_ID, chairmanHandler);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            stompSession.subscribe("/chairman/" + ROOM_ID, firstChairmanHandler);
            stompSession.subscribe("/room/" + ROOM_ID, existingAudienceHandler);
            firstChairmanHandler.getCompletableFuture().get(3L, TimeUnit.SECONDS); // 기존 청중 구독 처리 완료 대기
            sharingRoomRegistry.markFinished(ROOM_ID);
            stompSession.send(headers, playRequest);
            existingAudienceHandler.getCompletableFuture().get(3L, TimeUnit.SECONDS); // 사회자 이벤트 처리 완료 대기
            stompSession.subscribe("/chairman/" + ROOM_ID, secondChairmanHandler);

            stompSession.subscribe("/room/" + ROOM_ID, new MessageFrameHandler<>(SharingResponse.class));

            ChairmanSharingRequest sharingRequest = secondChairmanHandler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(sharingRequest.roomId()).isEqualTo(ROOM_ID);
        }
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

    private static class QueueFrameHandler<T> implements StompFrameHandler {

        private final BlockingQueue<T> messages = new LinkedBlockingQueue<>();
        private final Class<T> tClass;

        QueueFrameHandler(Class<T> tClass) {
            this.tClass = tClass;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return tClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders headers, Object payload) {
            messages.add((T) payload);
        }

        T poll(long timeoutSeconds) throws InterruptedException {
            return messages.poll(timeoutSeconds, TimeUnit.SECONDS);
        }
    }
}
