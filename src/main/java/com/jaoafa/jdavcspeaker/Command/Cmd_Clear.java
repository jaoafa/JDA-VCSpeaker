package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Cmd_Clear implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(this::clear)
                .build()
        );
    }

    void clear(CommandContext<JDACommandSender> context) {
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

        message.reply(new EmbedBuilder()
            .setTitle(":white_check_mark: 読み上げをクリアしました。")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
