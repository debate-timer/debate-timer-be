package com.debatetimer.client;

import com.debatetimer.exception.custom.DTInitializationException;
import com.debatetimer.exception.errorcode.InitializationErrorCode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(DiscordProperties.class)
public class DiscordNotifier {

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
        String errorMessage = throwable.getMessage();
        TextChannel channel = jda.getTextChannelById(properties.getChannelId());
        channel.sendMessage(":rotating_light:  [**Error 발생!**]\n```\n" + errorMessage + "\n```").queue();
    }
}

