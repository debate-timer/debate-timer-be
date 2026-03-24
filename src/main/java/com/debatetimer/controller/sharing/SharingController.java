package com.debatetimer.controller.sharing;

import com.debatetimer.dto.sharing.request.SharingRequest;
import com.debatetimer.dto.sharing.response.SharingResponse;
import com.debatetimer.dto.sharing.response.TimerEventInfoResponse;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SharingController {

    @MessageMapping("/event/{roomId}")
    @SendTo("/room/{roomId}")
    public SharingResponse share(
            @DestinationVariable(value = "roomId") long roomId,
            @Valid @Payload SharingRequest request
    ) {
        if (!request.hasEventData()) {
            return new SharingResponse(request.eventType(), null);
        }

        return new SharingResponse(
                request.eventType(),
                new TimerEventInfoResponse(request.toTimerEventInfo())
        );
    }
}
