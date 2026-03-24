package com.debatetimer.controller.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.BaseStompTest;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.sharing.TimerEventType;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.request.TimerEventInfoRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.messaging.simp.stomp.StompHeaders;

class SharingControllerTest extends BaseStompTest {

    @Nested
    class Share {

        @Test
        void 사회자가_발생시킨_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            long roomId = 1L;
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateAccessTokenHeader("/app/event/" + roomId, member);
            SharingRequest request = new SharingRequest(
                    TimerEventType.NEXT,
                    new TimerEventInfoRequest(
                            CustomizeBoxType.NORMAL,
                            2,
                            null,
                            30
                    )
            );
            stompSession.subscribe("/room/" + roomId, handler); //청중의 구독

            stompSession.send(headers, request); //사회자의 이벤트 발생

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(request.eventType()),
                    () -> assertThat(response.data().timerType()).isEqualTo(request.data().timerType()),
                    () -> assertThat(response.data().sequence()).isEqualTo(request.data().sequence()),
                    () -> assertThat(response.data().currentTeam()).isEqualTo(request.data().currentTeam()),
                    () -> assertThat(response.data().remainingTime()).isEqualTo(request.data().remainingTime())
            );
        }

        @Test
        void 사회자가_발생시킨_토론_종료_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            long roomId = 1L;
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateAccessTokenHeader("/app/event/" + roomId, member);
            SharingRequest request = new SharingRequest(TimerEventType.FINISHED, null);
            stompSession.subscribe("/room/" + roomId, handler); //청중의 구독

            stompSession.send(headers, request); //사회자의 이벤트 발생

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);

            assertAll(
                    () -> assertThat(response.eventType()).isEqualTo(request.eventType()),
                    () -> assertThat(response.data().timerType()).isEqualTo(request.data().timerType()),
                    () -> assertThat(response.data().sequence()).isEqualTo(request.data().sequence()),
                    () -> assertThat(response.data().currentTeam()).isEqualTo(request.data().currentTeam()),
                    () -> assertThat(response.data().remainingTime()).isEqualTo(request.data().remainingTime())
            );
        }

        @ParameterizedTest
        @NullSource
        void 타이머_타입은_빈_값일_수_없다(CustomizeBoxType boxType) {
            long roomId = 1L;
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateAccessTokenHeader("/app/event/" + roomId, member);
            SharingRequest request = new SharingRequest(
                    TimerEventType.NEXT,
                    new TimerEventInfoRequest(
                            boxType,
                            2,
                            null,
                            30
                    )
            );
            stompSession.subscribe("/room/" + roomId, handler); //청중의 구독

            stompSession.send(headers, request); //사회자의 이벤트 발생

            assertThatThrownBy(() -> handler.getCompletableFuture()
                    .get(2L, TimeUnit.SECONDS))
                    .isInstanceOf(TimeoutException.class);
        }
    }
}
