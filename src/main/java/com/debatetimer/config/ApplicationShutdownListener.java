package com.debatetimer.config;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationShutdownListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("========== APPLICATION SHUTDOWN STARTED ==========");
        log.info("Shutdown triggered at: {}", LocalDateTime.now());
        log.info("Active threads: {}", Thread.activeCount());
        log.info("========== GRACEFUL SHUTDOWN IN PROGRESS ==========");
    }
}
