package com.debatetimer.aop.logging;

import com.debatetimer.exception.custom.DTOAuthClientException;
import jakarta.servlet.http.HttpServletRequest;
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

    @Pointcut("@within(com.debatetimer.aop.logging.LoggingClient)")
    public void loggingClients() {
    }

    @Around("loggingClients()")
    public Object loggingControllerMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            logClientRequest(proceedingJoinPoint);
            Object responseBody = proceedingJoinPoint.proceed();
            logClientResponse(proceedingJoinPoint);
            return responseBody;
        } catch (DTOAuthClientException exception) {
            logClientErrorRequest(proceedingJoinPoint);
            logClientErrorResponse(exception.getErrorResponse());
            throw exception;
        }
    }

    private void logClientRequest(ProceedingJoinPoint joinPoint) {
        String clientName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("Client Request Logging - Client Name: {} | MethodName: {}", clientName, methodName);
    }

    private void logClientResponse(ProceedingJoinPoint joinPoint) {
        String clientName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("Client Response Logging - Client Name: {} | MethodName: {}", clientName, methodName);
    }

    private void logClientErrorRequest(ProceedingJoinPoint proceedingJoinPoint) {
        HttpServletRequest request = getHttpServletRequest();
        String requestParameters = getRequestParameters(proceedingJoinPoint);
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();
        log.info("Client Request Error Logging: {} {} parameters - {}", httpMethod, uri, requestParameters);
    }

    private void logClientErrorResponse(String responseBody) {
        HttpServletRequest request = getHttpServletRequest();
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();
        log.info("Client Response Error Logging - Client Name: {} | MethodName: {} | response_body: {}",
                httpMethod, uri, responseBody);
    }
}

