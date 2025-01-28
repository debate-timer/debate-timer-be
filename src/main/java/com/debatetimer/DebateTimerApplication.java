package com.debatetimer;

import com.debatetimer.client.OAuthProperties;
import com.debatetimer.service.auth.JwtTokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OAuthProperties.class, JwtTokenProperties.class})
public class DebateTimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebateTimerApplication.class, args);
    }

}
