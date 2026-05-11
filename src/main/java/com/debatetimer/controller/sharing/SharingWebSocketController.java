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
        return sharingService.share(request);
    }
}
