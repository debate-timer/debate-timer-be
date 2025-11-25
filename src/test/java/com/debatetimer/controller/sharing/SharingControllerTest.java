package com.debatetimer.controller.sharing;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.BaseStompTest;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompHeaders;

class SharingControllerTest extends BaseStompTest {

    @Nested
    class Share {

        @Test
        void 사회자가_발생시킨_이벤트를_청중이_공유받는다() throws ExecutionException, InterruptedException, TimeoutException {
            long roomId = 1L;
            LocalDateTime time = LocalDateTime.now();
            MessageFrameHandler<SharingResponse> handler = new MessageFrameHandler<>(SharingResponse.class);
            Member member = memberGenerator.generate("example@email.com");
            StompHeaders headers = headerGenerator.generateAccessTokenHeader("/app/event/" + roomId, member);
            stompSession.subscribe("/room/" + roomId, handler); //청중의 구독

            stompSession.send(headers, new SharingRequest(time)); //사회자의 이벤트 발생

            SharingResponse response = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(response.time()).isEqualTo(time);
        }
    }
}
