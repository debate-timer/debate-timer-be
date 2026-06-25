package com.debatetimer.config.sharing;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthMemberResolver implements HandlerMethodArgumentResolver {

    private final AuthManager authManager;
    private final AuthService authService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

        if (token == null) {
            log.warn("웹소켓 인증 실패: Authorization 헤더 없음, sessionId={}", accessor.getSessionId());
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }

        String email = authManager.resolveChairmanToken(token);
        log.info("웹소켓 인증 성공: sessionId={}", accessor.getSessionId());
        return authService.getMember(email);
    }
}

