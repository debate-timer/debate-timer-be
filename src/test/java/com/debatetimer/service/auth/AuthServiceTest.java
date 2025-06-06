package com.debatetimer.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.domain.member.Member;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthServiceTest extends BaseServiceTest {

    @Autowired
    private AuthService authService;

    @Nested
    class GetMember {

        @Test
        void 이메일에_해당하는_멤버가_있으면_해당_멤버를_반환한다() {
            Member bito = memberGenerator.generate("default@gmail.com");

            assertThat(authService.getMember(bito.getEmail()).getId()).isEqualTo(bito.getId());
        }
    }

    @Nested
    class Logout {

        @Test
        void 이메일과_멤버의_정보가_다르면_예외를_발생시킨다() {
            Member bito = memberGenerator.generate("bito@gmail.com");
            String email = "default@gmail.com";

            assertThatThrownBy(() -> authService.logout(bito, email))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.UNAUTHORIZED_MEMBER.getMessage());
        }
    }
}
