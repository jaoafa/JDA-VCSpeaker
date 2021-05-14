package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibIgnore;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.stream.Collectors;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Ignore implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                    .literal("add")
                    .literal("contain", "contains")
                    .argument(StringArgument.greedy("text"))
                        .handler(context -> execute(context, this::addContains))
                        .build(),
            builder
                .literal("add")
                .literal("equal", "equals")
                .argument(StringArgument.greedy("text"))
                        .handler(context -> execute(context, this::addEquals))
                        .build(),
            builder
                .literal("remove", "rm", "delete", "del")
                .literal("contain", "contains")
                .argument(StringArgument.greedy("text"))
                        .handler(context -> execute(context, this::removeContains))
                        .build(),
            builder
                .literal("remove", "rm", "delete", "del")
                .literal("equal", "equals")
                .argument(StringArgument.greedy("text"))
                        .handler(context -> execute(context, this::removeEquals))
                        .build(),
                builder
                        .literal("list")
                        .handler(context -> execute(context, this::list))
                        .build()
        );
    }

    void addContains(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        String text = context.getOrDefault("text", null);
        if (text == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: パラメーターが足りません！")
                    .setDescription("無視するテキストを入力してください。")
                    .addField(":beginner: EX:", "`;ignore add contains 含んでいたら無視するテキスト`", false)
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        LibIgnore.addToIgnore("contain", text);

        message.reply(new EmbedBuilder()
                .setTitle(":pencil: 無視項目を設定しました！")
                .setDescription(String.format("`%s`が含まれるメッセージは読み上げません。", text))
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }

    void addEquals(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        String text = context.getOrDefault("text", null);
        if (text == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: パラメーターが足りません！")
                    .setDescription("無視するテキストを入力してください。")
                    .addField(":beginner: EX:", "`;ignore add equals 含んでいたら無視するテキスト`", false)
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        LibIgnore.addToIgnore("equal", text);

        message.reply(new EmbedBuilder()
                .setTitle(":pencil: 無視項目を設定しました！")
                .setDescription(String.format("`%s`に一致するメッセージは読み上げません。", text))
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }

    void removeContains(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        String text = context.getOrDefault("text", null);
        if (text == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: パラメーターが足りません！")
                    .setDescription("無視するテキストを入力してください。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        LibIgnore.removeFromIgnore("contain", text);

        message.reply(new EmbedBuilder()
                .setTitle(":wastebasket: 無視項目を削除しました！")
                .setDescription(String.format("今後は`%s`が含まれているメッセージも読み上げます。", text))
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }

    void removeEquals(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!context.getSender().getEvent().isPresent()) {
            channel.sendMessage(new EmbedBuilder()
                    .setTitle(":warning: 何かがうまくいきませんでした…")
                    .setDescription("メッセージデータを取得できませんでした。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        String text = context.getOrDefault("text", null);
        if (text == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: パラメーターが足りません！")
                    .setDescription("textパラメーターが足りません。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        LibIgnore.removeFromIgnore("equal", text);

        message.reply(new EmbedBuilder()
                .setTitle(":wastebasket: 無視項目を削除しました！")
                .setDescription(String.format("今後は`%s`と一致するメッセージも読み上げます。", text))
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }

    void list(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!context.getSender().getEvent().isPresent()) {
            channel.sendMessage(new EmbedBuilder()
                    .setTitle(":warning: 何かがうまくいきませんでした…")
                    .setDescription("メッセージデータを取得できませんでした。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        String list = StaticData.ignoreMap.entrySet().stream()
                .map(entry -> String.format("`%s` : `%s`", entry.getKey(), entry.getValue())) // keyとvalueを繋げる
                .collect(Collectors.joining("\n")); // それぞれを改行で連結する

        message.reply(new EmbedBuilder()
                .setTitle(":bookmark_tabs: 現在の無視項目")
                .setDescription(list)
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }
}
