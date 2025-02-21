package com.debatetimer.config;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] corsOrigin;

    public CorsConfig(@Value("${cors.origin}") String[] corsOrigin) {
        validate(corsOrigin);
        this.corsOrigin = corsOrigin;
    }

    private void validate(String[] corsOrigin) {
        if (corsOrigin == null || corsOrigin.length == 0) {
            throw new DTInitializationException(InitializationErrorCode.CORS_ORIGIN_EMPTY);
        }
        for (String origin : corsOrigin) {
            if (origin == null || origin.isBlank()) {
                throw new DTInitializationException(InitializationErrorCode.CORS_ORIGIN_STRING_BLANK);
            }
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(corsOrigin)
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
                .allowCredentials(true)
                .allowedHeaders("*")
                .exposedHeaders(HttpHeaders.AUTHORIZATION);
    }
}

