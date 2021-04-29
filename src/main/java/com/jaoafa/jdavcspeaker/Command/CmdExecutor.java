package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.Lib.CmdFunction;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class CmdExecutor {
    public static void execute(CommandContext<JDACommandSender> context, CmdFunction handler) {
        MessageChannel channel = context.getSender().getChannel();
        if (!channel.getId().equals(StaticData.vcTextChannel)) return;
        if (!context.getSender().getEvent().isPresent()) {
            channel.sendMessage(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription("メッセージデータを取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        Guild guild = context.getSender().getEvent().get().getGuild();
        Member member = guild.getMember(context.getSender().getUser());
        Message message = context.getSender().getEvent().get().getMessage();
        if (!channel.getId().equals(StaticData.vcTextChannel)) return;
        handler.execute(guild, channel, member, message, context);
    }
}
