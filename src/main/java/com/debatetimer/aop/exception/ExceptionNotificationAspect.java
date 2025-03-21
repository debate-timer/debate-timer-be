package com.debatetimer.aop.exception;

import com.debatetimer.client.DiscordNotifier;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionNotificationAspect {

    private final DiscordNotifier discordNotifier;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllers() {
    }

    @AfterThrowing(pointcut = "restControllers()", throwing = "exception")
    public void sendDiscordNotification(JoinPoint joinPoint, Exception exception) {
        discordNotifier.sendErrorMessage(exception);

    }
}
