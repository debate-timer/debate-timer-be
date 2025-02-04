package com.debatetimer.controller.tool.cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.fixture.CookieGenerator;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CookieExtractorTest {

    private CookieGenerator cookieGenerator;
    private CookieExtractor cookieExtractor;

    @BeforeEach
    void setUp() {
        this.cookieGenerator = new CookieGenerator();
        this.cookieExtractor = new CookieExtractor();
    }

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
