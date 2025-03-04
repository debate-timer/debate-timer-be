package com.debatetimer.aop.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ClientLoggingAspect extends LoggingAspect {

    private static final String CLIENT_REQUEST_TIME_KEY = "clientRequestTime";

    @Pointcut("@within(com.debatetimer.aop.logging.Logging)")
    public void loggingClients() {
    }

    @Around("loggingClients()")
    public Object loggingControllerMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        setMdc(CLIENT_REQUEST_TIME_KEY, System.currentTimeMillis());
        logClientRequest(proceedingJoinPoint);

        Object responseBody = proceedingJoinPoint.proceed();

        logClientResponse(proceedingJoinPoint);
        removeMdc(CLIENT_REQUEST_TIME_KEY);
        return responseBody;
    }

    private void logClientRequest(ProceedingJoinPoint joinPoint) {
        String clientName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("Client Request Logging - Client Name: {} | MethodName: {}", clientName, methodName);
    }

    private void logClientResponse(ProceedingJoinPoint joinPoint) {
        String clientName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long latency = getLatency(CLIENT_REQUEST_TIME_KEY);
        log.info("Client Response Logging - Client Name: {} | MethodName: {} | Latency: {}ms",
                clientName, methodName, latency);
    }
}

