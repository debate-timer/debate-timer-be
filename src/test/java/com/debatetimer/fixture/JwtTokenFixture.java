package com.debatetimer.fixture;

import com.debatetimer.controller.tool.jwt.JwtTokenProperties;

public class JwtTokenFixture {

    public static final JwtTokenProperties TEST_TOKEN_PROPERTIES = new JwtTokenProperties(
            "test".repeat(8),
            5000,
            10000
    );
}
