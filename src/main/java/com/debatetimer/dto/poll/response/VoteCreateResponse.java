package com.debatetimer.dto.poll.response;

import com.debatetimer.domain.poll.VoteTeam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VoteCreateResponse(
        long id,
        @NotBlank String name,
        @NotBlank String participantCode,
        @NotNull VoteTeam team
) {
}
