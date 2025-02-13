package com.debatetimer.config;

import com.debatetimer.controller.tool.export.ExcelExportInterceptor;
import com.debatetimer.controller.tool.jwt.AuthManager;
import com.debatetimer.service.auth.AuthService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthManager authManager;
    private final AuthService authService;
    private final ExcelExportInterceptor excelExportInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthMemberArgumentResolver(authManager, authService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(excelExportInterceptor)
                .addPathPatterns("/api/table/**/export/**");
    }
}
