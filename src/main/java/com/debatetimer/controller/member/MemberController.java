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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";

    private final MemberService memberService;
    private final AuthService authService;
    private final CookieManager cookieManager;
    private final AuthManager authManager;

    @GetMapping("/api/table")
    public TableResponses getTables(@AuthMember Member member) {
        return memberService.getTables(member.getId());
    }

    @PostMapping("/api/member")
    public ResponseEntity<MemberCreateResponse> createMember(@RequestBody MemberCreateRequest request) {
        MemberInfo memberInfo = authService.getMemberInfo(request);
        MemberCreateResponse memberCreateResponse = memberService.createMember(memberInfo);
        JwtTokenResponse jwtToken = authManager.issueToken(memberInfo);
        ResponseCookie refreshTokenCookie = cookieManager.createCookie(REFRESH_TOKEN_COOKIE_KEY,
                jwtToken.refreshToken(), jwtToken.refreshExpiration());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, jwtToken.accessToken())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(memberCreateResponse);
    }

    @PostMapping("/api/member/reissue")
    public ResponseEntity<Void> reissueAccessToken(@CookieValue(REFRESH_TOKEN_COOKIE_KEY) String refreshToken) {
        JwtTokenResponse jwtToken = authManager.reissueToken(refreshToken);
        ResponseCookie refreshTokenCookie = cookieManager.createCookie(REFRESH_TOKEN_COOKIE_KEY,
                jwtToken.refreshToken(), jwtToken.refreshExpiration());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken.accessToken())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/api/member/logout")
    public ResponseEntity<Void> logout(@AuthMember Member member,
                                       @CookieValue(REFRESH_TOKEN_COOKIE_KEY) String refreshToken) {
        String email = authManager.resolveRefreshToken(refreshToken);
        authService.logout(member, email);
        ResponseCookie expiredRefreshTokenCookie = cookieManager.createExpiredCookie(REFRESH_TOKEN_COOKIE_KEY);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshTokenCookie.toString())
                .build();
    }
}
