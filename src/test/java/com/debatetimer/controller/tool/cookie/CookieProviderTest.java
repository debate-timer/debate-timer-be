package com.debatetimer.controller.tool.cookie;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

class CookieProviderTest {

    private static final String SAME_SITE_NONE_ATTRIBUTE = "SameSite=None";
    private static final String SECURE_ATTRIBUTE = "Secure";
    private static final String HTTP_ONLY_ATTRIBUTE = "HttpOnly";
    private static final String ROOT_PATH_ATTRIBUTE = "Path=/";

    @Nested
    class CreateCookie {

        @Test
        void 쿠키_이름과_값을_설정한다() {
            CookieProvider cookieProvider = new CookieProvider();
            String key = "cookieKey";
            String value = "tokenValue";

            ResponseCookie cookie = cookieProvider.createCookie(key, value, Duration.ofHours(1));

            assertThat(cookie.toString())
                    .contains("%s=%s;".formatted(key, value));
        }

        @Test
        void 클라이언트와_서버가_분리된_환경에서_쿠키가_정상작동하도록_설정한다() {
            CookieProvider cookieProvider = new CookieProvider();

            ResponseCookie cookie = cookieProvider.createCookie("key", "value", Duration.ofHours(1));

            assertThat(cookie.toString())
                    .contains(SAME_SITE_NONE_ATTRIBUTE)
                    .contains(SECURE_ATTRIBUTE)
                    .contains(HTTP_ONLY_ATTRIBUTE)
                    .contains(ROOT_PATH_ATTRIBUTE);
        }
    }
}
