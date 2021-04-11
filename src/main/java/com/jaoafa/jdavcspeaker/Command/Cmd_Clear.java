package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Cmd_Clear implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        PlayerManager.getINSTANCE().getGuildMusicManager(guild).scheduler.queue.clear();
        PlayerManager.getINSTANCE().getGuildMusicManager(guild).player.destroy();
        EmbedBuilder disconSuccess = new EmbedBuilder();
        disconSuccess.setTitle(":white_check_mark: 読み上げをクリアしました。");
        disconSuccess.setColor(LibEmbedColor.success);
        channel.sendMessage(disconSuccess.build()).queue();
    }
}
