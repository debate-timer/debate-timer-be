package com.debatetimer.controller.member;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.service.auth.AuthService;
import com.debatetimer.service.cookie.CookieService;
import com.debatetimer.service.member.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final CookieService cookieService;

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
        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(jwtTokenResponse.refreshToken());

        response.addHeader(HttpHeaders.AUTHORIZATION, jwtTokenResponse.accessToken());
        response.addCookie(refreshTokenCookie);
    }

    @PostMapping("/api/member/reissue")
    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.extractRefreshToken(request.getCookies());
        JwtTokenResponse jwtTokenResponse = authService.reissueToken(refreshToken);
        Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(jwtTokenResponse.refreshToken());

        response.addHeader(HttpHeaders.AUTHORIZATION, jwtTokenResponse.accessToken());
        response.addCookie(refreshTokenCookie);
    }
}
