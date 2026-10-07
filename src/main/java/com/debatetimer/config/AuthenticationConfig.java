package com.debatetimer.config;

import com.debatetimer.client.oauth.OAuthProperties;
import com.debatetimer.controller.admin.AdminProperties;
import com.debatetimer.controller.tool.jwt.JwtTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OAuthProperties.class, JwtTokenProperties.class, AdminProperties.class})
public class AuthenticationConfig {

}
