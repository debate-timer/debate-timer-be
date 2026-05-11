package com.debatetimer.config.sharing;

import com.debatetimer.config.CorsProperties;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final long SERVER_TO_CLIENT_HEARTBEAT_DURATION = Duration.ofSeconds(10).toMillis();
    private static final long CLIENT_TO_SERVER_HEARTBEAT_DURATION = Duration.ofSeconds(10).toMillis();
    private static final long SOCKJS_HEART_BEAT_DURATION = Duration.ofSeconds(10).toMillis();
    private static final String HEART_BEAT_THREAD_PREFIX = "wss-heartbeat-";
    private static final int HEART_BEAT_THREAD_COUNT = 1;

    private final CorsProperties corsProperties;
    private final WebSocketAuthMemberResolver webSocketAuthMemberResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(webSocketAuthMemberResolver);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/room", "/chairman")
                .setHeartbeatValue(new long[]{SERVER_TO_CLIENT_HEARTBEAT_DURATION, CLIENT_TO_SERVER_HEARTBEAT_DURATION})
                .setTaskScheduler(heartBeatScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(corsProperties.getCorsOrigin())
                .withSockJS()
                .setHeartbeatTime(SOCKJS_HEART_BEAT_DURATION);
    }

    @Bean
    public ThreadPoolTaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(HEART_BEAT_THREAD_COUNT);
        scheduler.setThreadNamePrefix(HEART_BEAT_THREAD_PREFIX);
        scheduler.initialize();
        return scheduler;
    }
}
