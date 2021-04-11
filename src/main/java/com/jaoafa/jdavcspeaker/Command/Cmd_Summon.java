package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

public class Cmd_Summon implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(this::summon)
                .build()
        );
    }

    void summon(CommandContext<JDACommandSender> context) {
        MessageChannel channel = context.getSender().getChannel();
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
        Message message = context.getSender().getEvent().get().getMessage();

        Guild guild = context.getSender().getEvent().get().getGuild();
        Member member = guild.getMember(context.getSender().getUser());
        if(member == null){
            message.reply(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription("memberを取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        if(member.getVoiceState() == null){
            message.reply(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription("VoiceStateを取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        VoiceChannel connectedChannel = member.getVoiceState().getChannel();
        if (connectedChannel == null) {
            message.reply(new EmbedBuilder()
                .setTitle(":warning: なにかがおかしいかも？")
                .setDescription("VCに参加してからVCSpeakerを呼び出してください。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(connectedChannel);

        message.reply(new EmbedBuilder()
            .setTitle(":white_check_mark: 接続しました！")
            .setDescription("`" + connectedChannel.getName() + "`に接続しました。")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
