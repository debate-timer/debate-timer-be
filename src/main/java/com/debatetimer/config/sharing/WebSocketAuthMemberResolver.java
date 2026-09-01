package com.debatetimer.config.sharing;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

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
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }

        String email = authManager.resolveChairmanToken(token);
        return authService.getMember(email);
    }
}

