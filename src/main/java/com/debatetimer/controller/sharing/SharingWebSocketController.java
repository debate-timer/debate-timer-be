package com.debatetimer.controller.sharing;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.service.sharing.SharingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SharingWebSocketController {

    private final SharingService sharingService;

    @MessageMapping("/event/{roomId}")
    @SendTo("/room/{roomId}")
    public SharingResponse share(
            @AuthMember Member member,
            @DestinationVariable(value = "roomId") long roomId,
            @Valid @Payload SharingRequest request
    ) {
        // null을 반환하면 @SendTo가 메시지를 보내지 않으므로, 거절된 이벤트는 청중에게 전달되지 않는다
        return sharingService.share(roomId, request)
                .orElse(null);
    }
}
