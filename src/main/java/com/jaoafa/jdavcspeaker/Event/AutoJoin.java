package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.text.MessageFormat;

public class AutoJoin extends ListenerAdapter {
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (event.getGuild().getSelfMember().getVoiceState() != null &&
            event.getGuild().getSelfMember().getVoiceState().getChannel() != null) {
            return; // 自身がいずれかのVCに参加している
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getChannelJoined());

        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":white_check_mark: AutoJoin")
            .setDescription(MessageFormat.format("<#{0}> に接続しました。", event.getChannelJoined().getId()))
            .setColor(LibEmbedColor.success);
        MultipleServer.getVCChannel(event.getGuild()).sendMessage(embed.build()).queue();
    }
}
