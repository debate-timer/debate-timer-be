package com.debatetimer.controller.tool.cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

class CookieProviderTest {

    @Nested
    class CreateCookie {

        @Test
        void 쿠키_이름과_값을_설정한다() {
            CookieProvider cookieProvider = new CookieProvider();
            String name = "cookieName";
            String token = "tokenValue";

            ResponseCookie cookie = cookieProvider.createCookie(name, token, 100_000);

            assertThat(cookie.toString())
                    .contains("%s=%s;".formatted(name, token));
        }

        @Test
        void 클라이언트와_서버가_분리된_환경에서_쿠키가_정상작동하도록_설정한다() {
            CookieProvider cookieProvider = new CookieProvider();

            ResponseCookie cookie = cookieProvider.createCookie("name", "token", 100_000);

            assertThat(cookie.toString())
                    .contains("SameSite=None")
                    .contains("Secure");
        }
    }

    @Nested
    class DeleteCookie {

        @Test
        void 값이_빈_만료된_쿠키를_생성한다() {
            CookieProvider cookieProvider = new CookieProvider();
            String name = "cookieName";

            ResponseCookie cookie = cookieProvider.deleteCookie(name);

            assertAll(
                    () -> assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO),
                    () -> assertThat(cookie.toString()).contains("%s=;".formatted(name))
            );
        }

        @Test
        void 클라이언트와_서버가_분리된_환경에서_쿠키가_정상작동하도록_설정한다() {
            CookieProvider cookieProvider = new CookieProvider();

            ResponseCookie cookie = cookieProvider.deleteCookie("name");

            assertThat(cookie.toString())
                    .contains("SameSite=None")
                    .contains("Secure");
        }
    }
}
