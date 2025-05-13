package com.debatetimer.aop.logging;


import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect extends LoggingAspect {

    private static final String REQUEST_ID_KEY = "requestId";

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void allController() {
    }

    @Around("allController()")
    public Object loggingControllerMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        setMdc(REQUEST_ID_KEY, UUID.randomUUID().toString());
        logControllerRequest(proceedingJoinPoint);

        Object responseBody = proceedingJoinPoint.proceed();

        logControllerResponse(responseBody);
        removeMdc(REQUEST_ID_KEY);
        return responseBody;
    }

    private void logControllerRequest(ProceedingJoinPoint proceedingJoinPoint) {
        HttpServletRequest request = getHttpServletRequest();
        String requestParameters = getRequestParameters(proceedingJoinPoint);
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();
        log.info("Request Logging: {} {} parameters - {}", httpMethod, uri, requestParameters);
    }

    private void logControllerResponse(Object responseBody) {
        HttpServletRequest request = getHttpServletRequest();
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();
        log.info("Response Logging: {} {} Body: {}", httpMethod, uri, responseBody);
    }
}
