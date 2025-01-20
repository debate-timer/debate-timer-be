package com.debatetimer.controller.member;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.service.auth.AuthService;
import com.debatetimer.service.member.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    @GetMapping("/api/table")
    public TableResponses getTables(@AuthMember Member member) {
        return memberService.getTables(member.getId());
    }

    @PostMapping("/api/member")
    @ResponseStatus(HttpStatus.CREATED)
    public void createMember(@RequestBody MemberCreateRequest request, HttpServletResponse response) {
        MemberInfo memberInfo = authService.getMemberInfo(request);
        memberService.createMember(memberInfo);
        JwtTokenResponse jwtTokenResponse = authService.createToken(memberInfo);
        response.addHeader(HttpHeaders.AUTHORIZATION, jwtTokenResponse.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtTokenResponse.refreshToken());
    }
}
