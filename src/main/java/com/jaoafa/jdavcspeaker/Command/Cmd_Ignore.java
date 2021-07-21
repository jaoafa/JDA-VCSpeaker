package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibIgnore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class Cmd_Ignore implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":expressionless:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "テキストを無視するように設定します")
                    .addSubcommandGroups(
                        new SubcommandGroupData("add", "設定を追加")
                            .addSubcommands(
                                new SubcommandData("contain", "内容を含むテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("equal", "内容に一致するテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true)
                            ),
                        new SubcommandGroupData("remove", "設定を消去")
                            .addSubcommands(
                                new SubcommandData("contain", "内容を含むテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("equal", "内容に一致するテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true)
                            )
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        switch (subCmd) {
            case "add:contain" -> addContains(event);
            case "add:equal" -> addEquals(event);
            case "remove:contain" -> removeContains(event);
            case "remove:equal" -> removeEquals(event);

        }
    }

    void addContains(SlashCommandEvent event) {
        String text = event.getOption("text").getAsString(/*絶対100%確実にRequired*/);
        LibIgnore.addToIgnore("contain", text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: 無視項目を設定しました！")
            .setDescription(String.format("`%s`が含まれるメッセージは読み上げません。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void addEquals(SlashCommandEvent event) {
        String text = event.getOption("text").getAsString(/*絶対100%確実にRequired*/);
        LibIgnore.addToIgnore("equal", text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: 無視項目を設定しました！")
            .setDescription(String.format("`%s`に一致するメッセージは読み上げません。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeContains(SlashCommandEvent event) {
        String text = event.getOption("text").getAsString(/*絶対100%確実にRequired*/);

        LibIgnore.removeFromIgnore("contain", text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: 無視項目を削除しました！")
            .setDescription(String.format("今後は`%s`が含まれているメッセージも読み上げます。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeEquals(SlashCommandEvent event) {
        String text = event.getOption("text").getAsString(/*絶対100%確実にRequired*/);
        LibIgnore.removeFromIgnore("equal", text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: 無視項目を削除しました！")
            .setDescription(String.format("今後は`%s`と一致するメッセージも読み上げます。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    //todo subcmdGroupと一緒にsubcmd置けないので 「/list alias/ignore」 的な感じで独立する
    /*
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

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":bookmark_tabs: 現在の無視項目")
            .setDescription(list)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }*/
}
