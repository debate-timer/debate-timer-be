package com.debatetimer.config;

import com.debatetimer.exception.decoder.H2ErrorDecoder;
import com.debatetimer.exception.decoder.MySqlErrorDecoder;
import com.debatetimer.exception.decoder.RepositoryErrorDecoder;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ErrorDecoderConfig {

    @Profile({"dev", "prod"})
    @Configuration
    public static class MySqlErrorDecoderConfig {

        @Bean
        public RepositoryErrorDecoder mySqlErrorDecoder() {
            return new MySqlErrorDecoder();
        }
    }

    @Profile({"test", "local"})
    @Configuration
    public static class H2ErrorDecoderConfig {

        @Bean
        public RepositoryErrorDecoder h2ErrorDecoder() {
            return new H2ErrorDecoder();
        }
    }
}
