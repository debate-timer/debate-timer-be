package com.debatetimer.config;

import com.debatetimer.controller.argumentresolver.AuthMemberArgumentResolver;
import com.debatetimer.repository.member.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MemberRepository memberRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthMemberArgumentResolver(memberRepository));
    }
}
