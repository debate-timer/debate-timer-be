package com.debatetimer.config;

import com.debatetimer.client.OAuthProperties;
import com.debatetimer.service.auth.JwtTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OAuthProperties.class, JwtTokenProperties.class})
public class AuthenticationConfig {

}
