package com.debatetimer.controller.member;

import com.debatetimer.controller.member.dto.MemberCreateRequest;
import com.debatetimer.controller.member.dto.MemberCreateResponse;
import com.debatetimer.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/member")
    public MemberCreateResponse createMember(@RequestBody MemberCreateRequest request) {
        return memberService.createMember(request);
    }
}
