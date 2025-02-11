package com.debatetimer.fixture;

import com.debatetimer.controller.tool.jwt.JwtTokenProperties;
import java.time.Duration;

public class JwtTokenFixture {

    public static final JwtTokenProperties TEST_TOKEN_PROPERTIES = new JwtTokenProperties(
            "test".repeat(8),
            Duration.ofMinutes(5L),
            Duration.ofMinutes(30L)
    );
}
