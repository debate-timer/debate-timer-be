package com.debatetimer.config.sharing;

import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.auth.AuthService;
import com.debatetimer.service.customize.CustomizeService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사회자 채널 구독 권한을 확인한다.
 * 사회자 채널을 구독하면 룸의 활성 사회자가 될 수 있으므로, 유효한 사회자 토큰을 가진 테이블 소유자만 허용한다.
 */
@Component
@RequiredArgsConstructor
public class ChairmanAuthorizer {

    private final AuthManager authManager;
    private final AuthService authService;
    private final CustomizeService customizeService;

    public void authorize(@Nullable String chairmanToken, long roomId) {
        if (chairmanToken == null || chairmanToken.isBlank()) {
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }
        String email = authManager.resolveChairmanToken(chairmanToken);
        Member member = authService.getMember(email);
        customizeService.validateTableOwner(roomId, member);
    }
}
