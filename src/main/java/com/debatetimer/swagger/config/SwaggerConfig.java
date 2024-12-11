package com.debatetimer.swagger.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(server())
                .info(getInfo());
    }

    private Server server() {
        return new Server().url("/");
    }

    private Info getInfo() {
        return new Info().title("디베이트 타이머 API 문서")
                .version("1.0.0");
    }
}
