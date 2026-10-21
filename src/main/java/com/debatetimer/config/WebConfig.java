package com.debatetimer.config;

import com.debatetimer.client.storage.IconStorageProperties;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.domain.organization.OrganizationIcon;
import com.debatetimer.service.auth.AuthService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthManager authManager;
    private final AuthService authService;
    private final IconStorageProperties iconStorageProperties;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthMemberArgumentResolver(authManager, authService));
    }

    // 업로드한 아이콘을 먼저 찾고, 없으면 배포에 포함된 기본 아이콘을 찾는다
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(OrganizationIcon.PATH_PREFIX + "**")
                .addResourceLocations(iconStorageProperties.getResourceLocation(), "classpath:/static/icon/");
    }
}
