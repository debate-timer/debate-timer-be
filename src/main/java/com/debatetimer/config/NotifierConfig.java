package com.debatetimer.config;


import com.debatetimer.client.notifier.ConsoleNotifier;
import com.debatetimer.client.notifier.DiscordNotifier;
import com.debatetimer.client.notifier.DiscordProperties;
import com.debatetimer.client.notifier.ErrorNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

public class NotifierConfig {

    private NotifierConfig() {
    }

    @Profile({"dev", "prod"})
    @Configuration
    @RequiredArgsConstructor
    @EnableConfigurationProperties(DiscordProperties.class)
    public static class DiscordNotifierConfig {

        private final DiscordProperties discordProperties;

        @Bean
        public ErrorNotifier discordNotifier() {
            return new DiscordNotifier(discordProperties);
        }
    }

    @Profile({"test", "local"})
    @Configuration
    public static class ConsoleNotifierConfig {

        @Bean
        public ErrorNotifier consoleNotifier() {
            return new ConsoleNotifier();
        }
    }
}
