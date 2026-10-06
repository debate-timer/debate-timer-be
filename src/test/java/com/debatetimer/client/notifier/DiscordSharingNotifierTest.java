package com.debatetimer.client.notifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.debatetimer.event.sharing.SharingFinishedEvent;
import java.time.LocalDateTime;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.Test;

class DiscordSharingNotifierTest {

    @Test
    void 공유_알림_채널로_공유_종료_메시지를_보낸다() {
        JDA jda = mock(JDA.class);
        TextChannel channel = mock(TextChannel.class);
        MessageCreateAction action = mock(MessageCreateAction.class);
        when(jda.getTextChannelById("sharing-channel")).thenReturn(channel);
        when(channel.sendMessage(anyString())).thenReturn(action);
        DiscordProperties properties = new DiscordProperties("token", "error-channel", "sharing-channel");
        LocalDateTime startedAt = LocalDateTime.of(2026, 10, 7, 10, 0);
        SharingFinishedEvent event = new SharingFinishedEvent(
                1L, 2L, "chairman@email.com", 3L, "테이블", startedAt, startedAt.plusMinutes(1), 60, 4);

        new DiscordSharingNotifier(properties, jda).sendSharingFinished(event);

        verify(channel).sendMessage(SharingFinishedMessage.from(event));
        verify(action).queue();
    }
}
