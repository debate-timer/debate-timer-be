package com.debatetimer.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 어드민 API 의 비밀번호를 요청 파라미터 바인딩보다 먼저 확인한다.
 * 비밀번호 없이 필수 파라미터까지 빠진 요청도 400 이 아니라 401 로 거절하기 위함이다.
 */
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_API_PATTERN = "/api/admin/**";
    public static final String ADMIN_PASSWORD_HEADER = "X-Admin-Password";

    private final AdminAuthorizer adminAuthorizer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        adminAuthorizer.authorize(request.getHeader(ADMIN_PASSWORD_HEADER));
        return true;
    }
}
