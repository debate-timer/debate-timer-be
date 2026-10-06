package com.debatetimer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 작업은 Spring Boot가 구성하는 기본 TaskExecutor(applicationTaskExecutor)에서 실행한다.
 * Executor 빈을 따로 등록하면 기본 TaskExecutor 구성이 빠지므로 직접 등록하지 않는다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

}
