package com.debatetimer.aop.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class LoggingAspect {

    protected final void setMdc(String key, Object value) {
        MDC.put(key, String.valueOf(value));
    }

    protected final void removeMdc(String key) {
        MDC.remove(key);
    }

    protected final long getLatency(String startTimeKey) {
        long startTime = Long.parseLong(MDC.get(startTimeKey));
        return System.currentTimeMillis() - startTime;
    }
}
