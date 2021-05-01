package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

public class Event_Join extends ListenerAdapter {
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        User user = event.getMember().getUser();
        VoiceChannel channel = event.getChannelJoined();
        if (StaticData.textChannel == null) return;
        StaticData.textChannel.sendMessage(MessageFormat.format(":inbox_tray: `{0}` が <#{1}> に参加しました。", user.getName(), channel.getId())).queue(
                message -> VoiceText.speak(message, MessageFormat.format("{0}が{1}に参加しました。", user.getName(), channel.getName()))
        );
    }
}
