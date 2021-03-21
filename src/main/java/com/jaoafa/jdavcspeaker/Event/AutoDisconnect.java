package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Util.EmbedColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class AutoDisconnect {
    @SubscribeEvent
    public void onMemberLeft(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getMembers().size()==1){
            if (event.getMember().getUser().isBot()){
                return;
            }
            event.getGuild().getAudioManager().closeAudioConnection();

            EmbedBuilder disconSuccess = new EmbedBuilder();
            disconSuccess.setTitle(":white_check_mark: AutoDisconnected");
            disconSuccess.setColor(EmbedColors.success);
            event.getJDA().getTextChannelById("623153228267388958").sendMessage(disconSuccess.build()).queue();
        }
    }
}
