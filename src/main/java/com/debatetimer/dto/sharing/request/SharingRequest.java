package com.debatetimer.dto.sharing.request;

import java.time.LocalDateTime;

public record SharingRequest(
        LocalDateTime time
) {

}
