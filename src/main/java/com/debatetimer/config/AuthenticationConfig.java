package com.debatetimer.config;

import com.debatetimer.client.oauth.OAuthProperties;
import com.debatetimer.controller.tool.jwt.JwtTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OAuthProperties.class, JwtTokenProperties.class})
public class AuthenticationConfig {

}
