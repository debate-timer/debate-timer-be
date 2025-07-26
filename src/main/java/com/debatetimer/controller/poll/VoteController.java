package com.debatetimer.controller.poll;

import com.debatetimer.dto.poll.request.VoteRequest;
import com.debatetimer.dto.poll.response.VoteCreateResponse;
import com.debatetimer.dto.poll.response.VoterPollInfoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoteController {

    @GetMapping("/api/polls/{pollId}/votes")
    @ResponseStatus(HttpStatus.OK)
    public VoterPollInfoResponse getVotersPollInfo(@PathVariable int pollId) {

        return null;
    }

    @PostMapping("/api/polls/{pollId}/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteCreateResponse votePoll(
            @PathVariable int pollId,
            @RequestBody @Valid VoteRequest voteRequest
    ) {

        return null;
    }
}
