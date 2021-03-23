package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Util.EmbedColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class AutoSummon {
    @SubscribeEvent
    public void onMemberJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()){
            return;
        }
        if (event.getGuild().getSelfMember().getVoiceState().getChannel() == null||event.getGuild().getSelfMember().getVoiceState().getChannel() == event.getChannelJoined()){
            return;
        }
        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getChannelJoined());

        EmbedBuilder joinSuccess = new EmbedBuilder();
        joinSuccess.setTitle(":white_check_mark: AutoJoined");
        joinSuccess.setDescription("`"+event.getChannelJoined().getName()+"`に接続しました。");
        joinSuccess.setColor(EmbedColors.success);
        event.getJDA().getTextChannelById("623153228267388958").sendMessage(joinSuccess.build()).queue();
    }
}
