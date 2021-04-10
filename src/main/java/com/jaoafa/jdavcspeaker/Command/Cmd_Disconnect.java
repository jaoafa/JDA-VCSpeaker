package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

public class Cmd_Disconnect implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        VoiceChannel connectedChannel = guild.getSelfMember().getVoiceState().getChannel();
        if (connectedChannel == null) {
            EmbedBuilder disconFailed = new EmbedBuilder();
            disconFailed.setTitle(":x: Error");
            disconFailed.setDescription("VCSpeakerはVCに参加していません。");
            disconFailed.setColor(LibEmbedColor.error);
            channel.sendMessage(disconFailed.build()).queue();
            return;
        }
        guild.getAudioManager().closeAudioConnection();

        EmbedBuilder disconSuccess = new EmbedBuilder();
        disconSuccess.setTitle(":white_check_mark: Disconnected");
        disconSuccess.setColor(LibEmbedColor.success);
        channel.sendMessage(disconSuccess.build()).queue();

    }
}
