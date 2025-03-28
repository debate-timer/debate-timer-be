package com.debatetimer.config;


import com.debatetimer.client.notifier.ConsoleNotifier;
import com.debatetimer.client.notifier.DiscordNotifier;
import com.debatetimer.client.notifier.DiscordProperties;
import com.debatetimer.client.notifier.ErrorNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class NotifierConfig {

    private final DiscordProperties discordProperties;

    @Profile({"dev", "prod"})
    @Bean
    public ErrorNotifier discordNotifier() {
        return new DiscordNotifier(discordProperties);
    }

    @Profile({"test", "local"})
    @Bean
    public ErrorNotifier consoleNotifier() {
        return new ConsoleNotifier();
    }
}
