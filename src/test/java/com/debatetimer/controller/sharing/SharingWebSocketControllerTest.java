package com.debatetimer.controller.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseStompTest;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.service.sharing.SharingRoomRegistry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;

class SharingWebSocketControllerTest extends BaseStompTest {

    private static final long ROOM_ID = 1L;

    @Autowired
    private SharingRoomRegistry sharingRoomRegistry;

    @AfterEach
    void reopenRoom() {
        sharingRoomRegistry.reopen(ROOM_ID);
    }

    @Nested
    class Share {

        @Test
        void 사회자가_발생시킨_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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

            stompSession.send(headers, request);

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(TimerEventType.PLAY),
                    () -> assertThat(response.version()).isEqualTo(request.version()),
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            stompSession.subscribe("/chairman/" + ROOM_ID, chairmanHandler);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
    class ShareSync {

        @Test
        void 사회자가_발생시킨_일반_타이머_동기화_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
            StompHeaders headers = headerGenerator.generateChairmanTokenHeader("/app/event/" + ROOM_ID, member);
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
}
