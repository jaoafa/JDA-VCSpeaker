package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.StaticData;
import com.jaoafa.jdavcspeaker.Util.EmbedColors;
import com.jaoafa.jdavcspeaker.Util.VoiceText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_Disconnect {
    @SubscribeEvent
    public void onMemberLeft(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().isBot()){
            return;
        }
        event.getJDA().getTextChannelById(StaticData.vcTextChannel).sendMessage(":outbox_tray: `"+event.getMember().getUser().getName()+"`が`"+event.getChannelLeft().getName()+"`から退出しました。").queue();
        VoiceText.speak(event.getJDA().getTextChannelById(StaticData.vcTextChannel),event.getMember().getUser().getName()+"が"+event.getChannelLeft().getName()+"から退出しました。",null);
    }
}
