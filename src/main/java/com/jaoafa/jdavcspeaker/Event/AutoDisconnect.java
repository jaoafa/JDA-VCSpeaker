package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoDisconnect extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        //VCに残ったメンバーが1人かつBot(VCSpeaker)
        if (event.getChannelLeft().getMembers().size() == 1 && event.getChannelLeft().getMembers().get(0).getUser().isBot()) {
            if (event.getMember().getUser().isBot()) {
                return;
            }
            event.getGuild().getAudioManager().closeAudioConnection();

            EmbedBuilder disconSuccess = new EmbedBuilder();
            disconSuccess.setTitle(":white_check_mark: AutoDisconnected");
            disconSuccess.setColor(LibEmbedColor.success);
            event.getJDA().getTextChannelById("623153228267388958").sendMessage(disconSuccess.build()).queue();
        }
    }
}
