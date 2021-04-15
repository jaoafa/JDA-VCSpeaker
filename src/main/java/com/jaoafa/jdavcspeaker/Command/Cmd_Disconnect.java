package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Disconnect implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(context -> execute(context, this::disconnect))
                .build()
        );
    }

    void disconnect(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if(!channel.getId().equals(StaticData.vcTextChannel)) return;
        if (!context.getSender().getEvent().isPresent()) {
            channel.sendMessage(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription("メッセージデータを取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        if(guild.getSelfMember().getVoiceState() == null){
            channel.sendMessage(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription("VoiceStateを取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        VoiceChannel connectedChannel = guild.getSelfMember().getVoiceState().getChannel();
        if (connectedChannel == null) {
            message.reply(new EmbedBuilder()
                .setTitle(":x: なにかがおかしいかも？")
                .setDescription("VCSpeakerはVCに参加していません...")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        guild.getAudioManager().closeAudioConnection();

        message.reply(new EmbedBuilder()
            .setTitle(":white_check_mark: 切断しました！")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
