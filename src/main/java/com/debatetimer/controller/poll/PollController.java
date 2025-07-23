package com.debatetimer.controller.poll;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.poll.response.PollCreateResponse;
import com.debatetimer.dto.poll.response.PollInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PollController {

    @PostMapping("/api/polls/{tableId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PollCreateResponse createPoll(
            @AuthMember Member member,
            @PathVariable(name = "tableId") long tableId
    ) {
        return null;
    }

    @GetMapping("/api/polls/{pollId}")
    @ResponseStatus(HttpStatus.OK)
    public PollInfoResponse readPollInfo(
            @AuthMember Member member,
            @PathVariable(name = "pollId") long pollId
    ) {
        return null;
    }

    @PatchMapping("/api/polls/{pollId}")
    @ResponseStatus(HttpStatus.OK)
    public PollInfoResponse endPoll(
            @AuthMember Member member,
            @PathVariable(name = "pollId") long pollId
    ) {
        return null;
    }
}
