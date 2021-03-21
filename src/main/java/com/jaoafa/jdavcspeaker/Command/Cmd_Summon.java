package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Util.EmbedColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

public class Cmd_Summon implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        VoiceChannel connectedChannel = member.getVoiceState().getChannel();
        if (connectedChannel == null) {
            EmbedBuilder joinFailed = new EmbedBuilder();
            joinFailed.setTitle(":x: Error");
            joinFailed.setDescription("VCに参加してからVCSpeakerを呼び出してください。");
            joinFailed.setColor(EmbedColors.error);
            channel.sendMessage(joinFailed.build()).queue();
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(connectedChannel);

        EmbedBuilder joinSuccess = new EmbedBuilder();
        joinSuccess.setTitle(":white_check_mark: Joined");
        joinSuccess.setDescription("`"+connectedChannel.getName()+"`に接続しました。");
        joinSuccess.setColor(EmbedColors.success);
        channel.sendMessage(joinSuccess.build()).queue();
    }
}
