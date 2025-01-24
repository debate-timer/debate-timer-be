package com.debatetimer.controller;

import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

public class GlobalControllerTest extends BaseControllerTest {

    @Value("${cors.origin}")
    private String corsOrigin;

    @Nested
    class CorsConfigTest {

        @Test
        void CORS_preflight에서_허용된_origin의_요청을_정상적으로_처리할_수_있다() {
            String allowedMethod = "GET";

            given()
                    .header("Origin", corsOrigin)
                    .header("Access-Control-Request-Method", allowedMethod)
                    .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                    .when().options("/")
                    .then().statusCode(200)
                    .headers("Access-Control-Allow-Origin", corsOrigin)
                    .header("Access-Control-Allow-Methods", containsString(allowedMethod));
        }

        @Test
        void CORS_preflight에서_허용되지_않은_origin의_요청을_막을_수_있다() {
            String notAllowedOrigin = "https://not-allowed-origin.com";
            String allowedMethod = "GET";

            given()
                    .header("Origin", notAllowedOrigin)
                    .header("Access-Control-Request-Method", allowedMethod)
                    .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                    .when().options("/")
                    .then().statusCode(403);
        }
    }
}
