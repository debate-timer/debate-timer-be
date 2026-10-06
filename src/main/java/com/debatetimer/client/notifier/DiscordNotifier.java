package com.debatetimer.client.notifier;

import io.micrometer.core.annotation.Timed;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Slf4j
public class DiscordNotifier implements ErrorNotifier {

    private static final String NOTIFICATION_PREFIX = ":rotating_light:  [**Error 발생!**]\n";
    private static final String STACK_TRACE_AFFIX = "\n```\n";
    private static final String DISCORD_LINE_SEPARATOR = "\n";
    private static final int STACK_TRACE_LENGTH = 10;

    private final DiscordProperties properties;
    private final JDA jda;

    public DiscordNotifier(DiscordProperties discordProperties, JDA jda) {
        this.properties = discordProperties;
        this.jda = jda;
    }

    @Timed(value = "discord.send_error_message")
    public void sendErrorMessage(Throwable throwable) {
        TextChannel channel = jda.getTextChannelById(properties.getChannelId());
        String errorMessage = throwable.toString();
        String stackTrace = getStackTraceAsString(throwable);

        String errorNotification = NOTIFICATION_PREFIX
                + errorMessage
                + STACK_TRACE_AFFIX
                + stackTrace
                + STACK_TRACE_AFFIX;
        channel.sendMessage(errorNotification).queue();
    }

    private String getStackTraceAsString(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .limit(STACK_TRACE_LENGTH)
                .collect(Collectors.joining(DISCORD_LINE_SEPARATOR));
    }
}
