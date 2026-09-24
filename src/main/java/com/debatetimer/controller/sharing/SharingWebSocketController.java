package com.debatetimer.controller.sharing;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.sharing.SharingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SharingWebSocketController {

    public static final String CHAIRMAN_SESSION_HEADER = "X-Chairman-Session";

    private final SharingService sharingService;

    @MessageMapping("/event/{roomId}")
    public void share(
            @AuthMember Member member,
            @DestinationVariable(value = "roomId") long roomId,
            @Header(name = CHAIRMAN_SESSION_HEADER, required = false) String chairmanSessionId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String simpSessionId,
            @Valid @Payload SharingRequest request
    ) {
        if (chairmanSessionId == null || chairmanSessionId.isBlank()) {
            throw new DTClientErrorException(ClientErrorCode.CHAIRMAN_SESSION_REQUIRED);
        }
        sharingService.share(roomId, chairmanSessionId, simpSessionId, request);
    }
}
