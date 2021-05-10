package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

public class Event_Join extends ListenerAdapter {
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        User user = event.getMember().getUser();
        VoiceChannel channel = event.getChannelJoined();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        MultipleServer.getVCChannel(event.getGuild()).sendMessage(MessageFormat.format(":inbox_tray: `{0}` が <#{1}> に参加しました。", user.getName(), channel.getId())).queue(
            message -> VoiceText.speak(message, MessageFormat.format("{0}が{1}に参加しました。", user.getName(), channel.getName()))
        );
    }
}
