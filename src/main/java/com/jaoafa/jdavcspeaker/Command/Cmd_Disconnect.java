package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Util.EmbedColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

public class Cmd_Disconnect implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        VoiceChannel connectedChannel = guild.getSelfMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            EmbedBuilder disconFailed = new EmbedBuilder();
            disconFailed.setTitle(":x: Error");
            disconFailed.setDescription("VCSpeakerはVCに参加していません。");
            disconFailed.setColor(EmbedColors.error);
            channel.sendMessage(disconFailed.build()).queue();
            return;
        }
        guild.getAudioManager().closeAudioConnection();

        EmbedBuilder disconSuccess = new EmbedBuilder();
        disconSuccess.setTitle(":white_check_mark: Disconnected");
        disconSuccess.setColor(EmbedColors.success);
        channel.sendMessage(disconSuccess.build()).queue();

    }
}
