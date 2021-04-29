package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibAlias;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.stream.Collectors;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Alias implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .literal("add")
                .argument(StringArgument.quoted("from"))
                .argument(StringArgument.quoted("to"))
                .handler(context -> execute(context, this::addAlias))
                .build(),
            builder
                .literal("remove", "rm", "delete", "del")
                .argument(StringArgument.quoted("from"))
                .handler(context -> execute(context, this::removeAlias))
                .build(),
            builder
                .literal("list")
                .handler(context -> execute(context, this::listAlias))
                .build()
        );
    }

    void addAlias(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!channel.getId().equals(StaticData.vcTextChannel)) return;

        String from = context.getOrDefault("from", null);
        String to = context.getOrDefault("to", null);
        if (from == null) {
            message.reply(new EmbedBuilder()
                .setTitle(":warning: パラメーターが足りません！")
                .setDescription("fromパラメーターが足りません。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        if (to == null) {
            message.reply(new EmbedBuilder()
                .setTitle(":warning: パラメーターが足りません！")
                .setDescription("toパラメーターが足りません。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        LibAlias.addToAlias(from, to);

        message.reply(new EmbedBuilder()
            .setTitle(":pencil: エイリアスを設定しました！")
            .setDescription(String.format("`%s`を`%s`に置き換えて読み上げます。", from, to))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeAlias(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
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

        String from = context.getOrDefault("from", null);
        if (from == null) {
            message.reply(new EmbedBuilder()
                .setTitle(":warning: パラメーターが足りません！")
                .setDescription("fromパラメーターが足りません。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        LibAlias.removeFromAlias(from);

        message.reply(new EmbedBuilder()
            .setTitle(":wastebasket: エイリアスを削除しました！")
            .setDescription(String.format("`%s`の置き換えを削除しました。", from))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void listAlias(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
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

        String list = StaticData.aliasMap.entrySet().stream()
            .map(entry -> String.format("`%s` -> `%s`", entry.getKey(), entry.getValue())) // keyとvalueを繋げる
            .collect(Collectors.joining("\n")); // それぞれを改行で連結する

        message.reply(new EmbedBuilder()
            .setTitle(":bookmark_tabs: 現在のエイリアス")
            .setDescription(list)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
