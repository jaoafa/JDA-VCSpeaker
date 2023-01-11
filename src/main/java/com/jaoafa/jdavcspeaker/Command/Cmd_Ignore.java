package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibIgnore;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.stream.Collectors;

public class Cmd_Ignore implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":expressionless:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "テキストを無視するように設定します")
                    .addSubcommandGroups(
                        new SubcommandGroupData("add", "設定を追加")
                            .addSubcommands(
                                new SubcommandData("contain", "内容を含むテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("equal", "内容に一致するテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("regex", "正規表現にマッチするテキストの無視設定")
                                    .addOption(OptionType.STRING, "regex", "正規表現", true)
                            ),
                        new SubcommandGroupData("remove", "設定を消去")
                            .addSubcommands(
                                new SubcommandData("contain", "内容を含むテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("equal", "内容に一致するテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("regex", "正規表現にマッチするテキストの無視設定")
                                    .addOption(OptionType.STRING, "regex", "正規表現", true)
                            ),
                        new SubcommandGroupData("list", "設定の閲覧")
                            .addSubcommands(
                                new SubcommandData("type", "無視設定の種別")
                                    .addOptions(new OptionData(OptionType.STRING, "type", "無視設定の種別（contain equal regex）を指定します", true)
                                        .addChoice("含む (contain)", "contain")
                                        .addChoice("一致 (equal)", "equal")
                                        .addChoice("正規表現 (regex)", "regex"))
                            )
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        switch (subCmd) {
            case "add:contain" -> addContains(event);
            case "add:equal" -> addEquals(event);
            case "add:regex" -> addRegex(event);
            case "remove:contain" -> removeContains(event);
            case "remove:equal" -> removeEquals(event);
            case "remove:regex" -> removeRegex(event);
            case "list:type" -> list(event);
        }
    }

    void addContains(SlashCommandInteractionEvent event) {
        String text = Main.getExistsOption(event, "text").getAsString();

        LibIgnore.addToContainIgnore(text);
        cmdFlow.success("%s が含有除外設定しました: %s", event.getUser().getAsTag(), text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: 無視項目を設定しました！")
            .setDescription(String.format("`%s`が含まれるメッセージは読み上げません。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void addEquals(SlashCommandInteractionEvent event) {
        String text = Main.getExistsOption(event, "text").getAsString();

        LibIgnore.addToEqualIgnore(text);
        cmdFlow.success("%s が一致除外設定しました: %s", event.getUser().getAsTag(), text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: 無視項目を設定しました！")
            .setDescription(String.format("`%s`に一致するメッセージは読み上げません。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void addRegex(SlashCommandInteractionEvent event) {
        String regex = Main.getExistsOption(event, "regex").getAsString();

        try {
            LibIgnore.addToRegexIgnore(regex);
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: 正規表現の構文エラー")
                .setDescription("正規表現の構文が不適切です。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        cmdFlow.success("%s が正規表現除外設定しました: %s", event.getUser().getAsTag(), regex);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: 無視項目を設定しました！")
            .setDescription(String.format("`%s`に正規表現でマッチするメッセージは読み上げません。", regex))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeContains(SlashCommandInteractionEvent event) {
        String text = Main.getExistsOption(event, "text").getAsString();

        LibIgnore.removeToContainIgnore(text);
        cmdFlow.success("%s が含有除外設定を解除しました: %s", event.getUser().getAsTag(), text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: 無視項目を削除しました！")
            .setDescription(String.format("今後は`%s`が含まれているメッセージも読み上げます。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeEquals(SlashCommandInteractionEvent event) {
        String text = Main.getExistsOption(event, "text").getAsString();

        LibIgnore.removeToEqualIgnore(text);
        cmdFlow.success("%s が一致除外設定を解除しました: %s", event.getUser().getAsTag(), text);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: 無視項目を削除しました！")
            .setDescription(String.format("今後は`%s`と一致するメッセージも読み上げます。", text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeRegex(SlashCommandInteractionEvent event) {
        String regex = Main.getExistsOption(event, "regex").getAsString();

        LibIgnore.removeToRegexIgnore(regex);
        cmdFlow.success("%s が正規表現除外設定を解除しました: %s", event.getUser().getAsTag(), regex);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: 無視項目を削除しました！")
            .setDescription(String.format("今後は`%s`と正規表現で一致するメッセージも読み上げます。", regex))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void list(SlashCommandInteractionEvent event) {
        String type = Main.getExistsOption(event, "type").getAsString();

        String list;
        switch (type) {
            case "contain" ->
                list = LibIgnore.contains.stream().map(s -> "`" + s + "`").collect(Collectors.joining("\n"));
            case "equal" -> list = LibIgnore.equals.stream().map(s -> "`" + s + "`").collect(Collectors.joining("\n"));
            case "regex" -> list = LibIgnore.regexs.stream().map(s -> "`" + s + "`").collect(Collectors.joining("\n"));
            default -> {
                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":x: 指定された type が正しくありません")
                    .setDescription("type には `contain` か `equal` か `regex` を指定できます。")
                    .setColor(LibEmbedColor.success)
                    .build()
                ).queue();
                return;
            }
        }

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":bookmark_tabs: 現在の無視項目")
            .setDescription(list)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
