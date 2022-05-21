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
                                    .addOption(OptionType.STRING, "text", "内容", true)
                            ),
                        new SubcommandGroupData("remove", "設定を消去")
                            .addSubcommands(
                                new SubcommandData("contain", "内容を含むテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true),
                                new SubcommandData("equal", "内容に一致するテキストの無視設定")
                                    .addOption(OptionType.STRING, "text", "内容", true)
                            ),
                        new SubcommandGroupData("list", "設定の閲覧")
                            .addSubcommands(
                                new SubcommandData("type", "無視設定の種別")
                                    .addOptions(new OptionData(OptionType.STRING, "type", "無視設定の種別（contain、もしくはequal）を指定します", true)
                                        .addChoice("含む (contain)", "contain")
                                        .addChoice("一致 (equal)", "equal"))
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
            case "remove:contain" -> removeContains(event);
            case "remove:equal" -> removeEquals(event);
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

    void list(SlashCommandInteractionEvent event) {
        String type = Main.getExistsOption(event, "type").getAsString();

        String list;
        if (type.equals("contain")) {
            list = String.join("\n", LibIgnore.contains);
        } else if (type.equals("equal")) {
            list = String.join("\n", LibIgnore.equals);
        } else {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: 指定された type が正しくありません")
                .setDescription("type には `contain` と `equal` を指定できます。")
                .setColor(LibEmbedColor.success)
                .build()
            ).queue();
            return;
        }

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":bookmark_tabs: 現在の無視項目")
            .setDescription(list)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
