package com.debatetimer.client;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"dev", "prod"})
@Component
@EnableConfigurationProperties(DiscordProperties.class)
public class DiscordNotifier {
    private static final String NOTIFICATION_PREFIX = ":rotating_light:  [**Error 발생!**]\n```\n";
    private static final int STACK_TRACE_LENGTH = 10;

    private final DiscordProperties properties;
    private final JDA jda;

    public DiscordNotifier(DiscordProperties discordProperties) {
        this.properties = discordProperties;
        this.jda = initializeJda(properties.getToken());
    }

    private JDA initializeJda(String token) {
        try {
            return JDABuilder.createDefault(token).build().awaitReady();
        } catch (InterruptedException e) {
            throw new DTInitializationException(InitializationErrorCode.JDA_INITIALIZATION_FAIL);
        }
    }

    public void sendErrorMessage(Throwable throwable) {
        TextChannel channel = jda.getTextChannelById(properties.getChannelId());
        String errorMessage = throwable.getMessage();
        String stackTrace = getStackTraceAsString(throwable);

        String errorNotification = NOTIFICATION_PREFIX
                + errorMessage
                + System.lineSeparator()
                + stackTrace;
        channel.sendMessage(errorNotification).queue();
    }

    private String getStackTraceAsString(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .limit(STACK_TRACE_LENGTH)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}

