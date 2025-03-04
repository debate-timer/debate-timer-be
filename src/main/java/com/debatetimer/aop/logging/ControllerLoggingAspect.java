package com.debatetimer.aop.logging;


import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect extends LoggingAspect {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String START_TIME_KEY = "startTime";

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void allController() {
    }

    @Around("allController()")
    public Object loggingControllerMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        setMdc(REQUEST_ID_KEY, UUID.randomUUID().toString());
        setMdc(START_TIME_KEY, System.currentTimeMillis());
        logControllerRequest(proceedingJoinPoint);

        Object responseBody = proceedingJoinPoint.proceed();

        logControllerResponse(responseBody);
        removeMdc(START_TIME_KEY);
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
        long latency = getLatency(START_TIME_KEY);
        log.info("Response Logging: {} {} Body: {} latency - {}ms", httpMethod, uri, responseBody, latency);
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    private String getRequestParameters(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] parameterNames = codeSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i], args[i]);
        }
        return params.toString();
    }
}
