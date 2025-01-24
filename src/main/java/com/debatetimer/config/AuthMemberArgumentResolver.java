package com.debatetimer.config;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.custom.DTException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
public class AuthMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthMember.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        try {
            long memberId = Long.parseLong(webRequest.getParameter("memberId"));
            return memberRepository.getById(memberId);
        } catch (DTException | NumberFormatException exception) {
            log.warn(exception.getMessage());
            throw new DTClientErrorException(ClientErrorCode.UNAUTHORIZED_MEMBER);
        }
    }
}


