package com.debatetimer.service.cookie;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CookieExtractorTest extends BaseServiceTest {

    @Autowired
    private CookieExtractor cookieExtractor;

    @Nested
    class ExtractCookie {

        @Test
        void 쿠키에서_해당하는_키의_값을_추출한다() {
            String key = "key";
            String value = "value";
            Cookie[] cookies = cookieGenerator.generateCookie(key, value, 100000);

            assertThat(cookieExtractor.extractCookie(key, cookies)).isEqualTo(value);
        }

        @Test
        void 쿠키에서_해당하는_값이_없으면_예외를_발생시킨다() {
            String key = "key";
            String value = "value";
            Cookie[] cookies = cookieGenerator.generateCookie("token", value, 100000);

            assertThatThrownBy(() -> cookieExtractor.extractCookie(key, cookies))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EMPTY_COOKIE.getMessage());
        }
    }
}