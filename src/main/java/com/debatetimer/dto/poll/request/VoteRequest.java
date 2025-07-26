package com.debatetimer.dto.poll.request;

import com.debatetimer.domain.poll.VoteTeam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(
        @NotBlank String name,
        @NotBlank String participantCode,
        @NotNull VoteTeam team
) {

}
