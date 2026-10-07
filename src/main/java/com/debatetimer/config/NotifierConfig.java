package com.debatetimer.config;


import com.debatetimer.client.notifier.ConsoleNotifier;
import com.debatetimer.client.notifier.ConsoleSharingNotifier;
import com.debatetimer.client.notifier.DiscordNotifier;
import com.debatetimer.client.notifier.DiscordProperties;
import com.debatetimer.client.notifier.DiscordSharingNotifier;
import com.debatetimer.client.notifier.ErrorNotifier;
import com.debatetimer.client.notifier.SharingNotifier;
import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class NotifierConfig {

    @Profile({"dev", "prod"})
    @Configuration
    @RequiredArgsConstructor
    @EnableConfigurationProperties(DiscordProperties.class)
    public static class DiscordNotifierConfig {

        private final DiscordProperties discordProperties;

        @Bean(destroyMethod = "shutdown")
        public JDA jda() {
            try {
                return JDABuilder.createDefault(discordProperties.getToken()).build().awaitReady();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DTInitializationException(InitializationErrorCode.JDA_INITIALIZATION_FAIL);
            }
        }

        @Bean
        public ErrorNotifier discordNotifier(JDA jda) {
            return new DiscordNotifier(discordProperties, jda);
        }

        @Bean
        public SharingNotifier discordSharingNotifier(JDA jda) {
            return new DiscordSharingNotifier(discordProperties, jda);
        }
    }

    @Profile({"test", "local"})
    @Configuration
    public static class ConsoleNotifierConfig {

        @Bean
        public ErrorNotifier consoleNotifier() {
            return new ConsoleNotifier();
        }

        @Bean
        public SharingNotifier consoleSharingNotifier() {
            return new ConsoleSharingNotifier();
        }
    }
}
