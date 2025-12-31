package com.debatetimer.event.sharing;

import static org.assertj.core.api.Assertions.assertThat;

import com.debatetimer.BaseStompTest;
import com.debatetimer.MessageFrameHandler;
import com.debatetimer.dto.sharing.request.ChairmanSharingRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RoomSubscribeListenerTest extends BaseStompTest {

    @Nested
    class SubscribeListener {

        @Test
        void 새로운_청중이_공유되면_사회자에게_정보공유_트리거를_발송한다() throws ExecutionException, InterruptedException, TimeoutException {
            long roomId = 1L;
            MessageFrameHandler<ChairmanSharingRequest> handler = new MessageFrameHandler<>(
                    ChairmanSharingRequest.class);
            stompSession.subscribe("/chairman/" + roomId, handler);

            stompSession.subscribe("/room/" + roomId, handler);

            ChairmanSharingRequest sharingRequest = handler.getCompletableFuture()
                    .get(3L, TimeUnit.SECONDS);
            assertThat(sharingRequest).isNotNull();
        }
    }
}
