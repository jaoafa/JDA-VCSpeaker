package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.StaticData;
import com.jaoafa.jdavcspeaker.Util.EmbedColors;
import com.jaoafa.jdavcspeaker.Util.VoiceText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Event_Move {
    @SubscribeEvent
    public void onMemberMove(GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().isBot()){
            return;
        }
        event.getJDA().getTextChannelById(StaticData.vcTextChannel).sendMessage(":twisted_rightwards_arrows: `"+event.getMember().getUser().getName()+"`が`"+event.getChannelLeft().getName()+"`から`"+event.getChannelJoined().getName()+"`に移動しました。").queue();
        VoiceText.speak(event.getJDA().getTextChannelById(StaticData.vcTextChannel),event.getMember().getUser().getName()+"が"+event.getChannelLeft().getName()+"から"+event.getChannelJoined().getName()+"に移動しました。");
    }
}
