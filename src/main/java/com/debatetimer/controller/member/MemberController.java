package com.debatetimer.controller.member;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.controller.tool.cookie.CookieManager;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.member.JwtTokenResponse;
import com.debatetimer.dto.member.MemberCreateRequest;
import com.debatetimer.dto.member.MemberCreateResponse;
import com.debatetimer.dto.member.MemberInfo;
import com.debatetimer.dto.member.TableResponses;
import com.debatetimer.service.auth.AuthService;
import com.debatetimer.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    private final CookieManager cookieManager;
    private final AuthManager authManager;

    @GetMapping("/api/table")
    public TableResponses getTables(@AuthMember Member member) {
        return memberService.getTables(member.getId());
    }

    @PostMapping("/api/member")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberCreateResponse createMember(@RequestBody MemberCreateRequest request, HttpServletResponse response) {
        MemberInfo memberInfo = authService.getMemberInfo(request);
        MemberCreateResponse memberCreateResponse = memberService.createMember(memberInfo);
        JwtTokenResponse jwtTokenResponse = authManager.issueToken(memberInfo);
        ResponseCookie refreshTokenCookie = cookieManager.createRefreshTokenCookie(jwtTokenResponse.refreshToken());

        response.addHeader(HttpHeaders.AUTHORIZATION, jwtTokenResponse.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return memberCreateResponse;
    }

    @PostMapping("/api/member/reissue")
    public void reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieManager.extractRefreshToken(request.getCookies());
        JwtTokenResponse jwtTokenResponse = authManager.reissueToken(refreshToken);
        ResponseCookie refreshTokenCookie = cookieManager.createRefreshTokenCookie(jwtTokenResponse.refreshToken());

        response.addHeader(HttpHeaders.AUTHORIZATION, jwtTokenResponse.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    @PostMapping("/api/member/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthMember Member member, HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieManager.extractRefreshToken(request.getCookies());
        String email = authManager.resolveRefreshToken(refreshToken);
        authService.logout(member, email);
        ResponseCookie deletedRefreshTokenCookie = cookieManager.deleteRefreshTokenCookie();

        response.addHeader(HttpHeaders.SET_COOKIE, deletedRefreshTokenCookie.toString());
    }
}
