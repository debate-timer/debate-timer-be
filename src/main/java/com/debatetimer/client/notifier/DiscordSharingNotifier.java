package com.debatetimer.client.notifier;

import com.debatetimer.event.sharing.SharingFinishedEvent;
import io.micrometer.core.annotation.Timed;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class DiscordSharingNotifier implements SharingNotifier {

    private final DiscordProperties properties;
    private final JDA jda;

    public DiscordSharingNotifier(DiscordProperties properties, JDA jda) {
        this.properties = properties;
        this.jda = jda;
    }

    @Timed(value = "discord.send_sharing_finished")
    @Override
    public void sendSharingFinished(SharingFinishedEvent event) {
        TextChannel channel = jda.getTextChannelById(properties.getSharingChannelId());
        channel.sendMessage(SharingFinishedMessage.from(event)).queue();
    }
}
