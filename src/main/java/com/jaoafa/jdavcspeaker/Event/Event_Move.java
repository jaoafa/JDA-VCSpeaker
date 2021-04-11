package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_Move {
    @SubscribeEvent
    public void onMemberMove(GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        event.getJDA().getTextChannelById(StaticData.vcTextChannel).sendMessage(":twisted_rightwards_arrows: `" + event.getMember().getUser().getName() + "`が`" + event.getChannelLeft().getName() + "`から`" + event.getChannelJoined().getName() + "`に移動しました。").queue();
        VoiceText.speak(event.getJDA().getTextChannelById(StaticData.vcTextChannel), event.getMember().getUser().getName() + "が" + event.getChannelLeft().getName() + "から" + event.getChannelJoined().getName() + "に移動しました。", null);
    }
}
