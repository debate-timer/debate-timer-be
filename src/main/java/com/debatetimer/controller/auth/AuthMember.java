package com.debatetimer.controller.auth;

import com.debatetimer.domain.member.FeatureFlag;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthMember {

    // 회원에게 모두 부여되어 있어야 하는 피쳐 플래그. 하나라도 없으면 403 으로 거절한다.
    FeatureFlag[] requires() default {};
}
