package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Event_Join extends ListenerAdapter {
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        event.getJDA().getTextChannelById(StaticData.vcTextChannel).sendMessage(":inbox_tray: `" + event.getMember().getUser().getName() + "`が`" + event.getChannelJoined().getName() + "`に参加しました。").queue();
        VoiceText.speak(event.getJDA().getTextChannelById(StaticData.vcTextChannel), event.getMember().getUser().getName() + "が" + event.getChannelJoined().getName() + "に参加しました。", null);
    }
}
