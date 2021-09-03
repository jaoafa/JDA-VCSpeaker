package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibAlias;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Lib.LibValue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Map;
import java.util.stream.Collectors;

public class Cmd_Alias implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":repeat:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "読みのエイリアスを設定します")
                    .addSubcommands(
                        new SubcommandData("add", "エイリアスを作成します")
                            .addOption(OptionType.STRING, "from", "変換元テキスト", true)
                            .addOption(OptionType.STRING, "to", "変換先テキスト", true),
                        new SubcommandData("remove", "エイリアスを削除します")
                            .addOption(OptionType.STRING, "from", "変換を削除するテキスト", true),
                        new SubcommandData("list", "エイリアス一覧を表示します"),
                        new SubcommandData("parse", "エイリアスを適用したテキストを返します")
                            .addOption(OptionType.STRING, "text", "エイリアスを適用するテキスト", true)
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        switch (subCmd) {
            case "add" -> addAlias(event, user);
            case "remove" -> removeAlias(event);
            case "list" -> listAlias(event);
            case "parse" -> parseAlias(event);
        }
    }

    void addAlias(SlashCommandEvent event, User user) {
        String from = Main.getExistsOption(event, "from").getAsString();
        String to = Main.getExistsOption(event, "to").getAsString();

        LibAlias.addToAlias(from, to);

        cmdFlow.success("%s がエイリアスを設定しました: %s -> %s", event.getUser().getAsTag(), from, to);
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: エイリアスを設定しました！")
            .setDescription("%s により追加".formatted(user.getAsMention()))
            .addField(":repeat: 置き換え", "`%s` → `%s`".formatted(from, to), false)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeAlias(SlashCommandEvent event) {
        String from = Main.getExistsOption(event, "from").getAsString();

        if (!LibValue.aliasMap.containsKey(from)) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":mag_right: エイリアスが見つかりませんでした！")
                .setDescription("""
                    `%s`に一致するエイリアスが見つかりませんでした。
                    `/alias list`で現在のエイリアスを確認することが出来ます。
                    """.formatted(from))
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        LibAlias.removeFromAlias(from);
        cmdFlow.success("%s がエイリアスを削除しました: %s -> %s", event.getUser().getAsTag(), from);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: エイリアスを削除しました！")
            .setDescription("%s により削除".formatted(from))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void listAlias(SlashCommandEvent event) {
        String list = LibValue.aliasMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> "`%s` -> `%s`".formatted(entry.getKey(), entry.getValue())) // keyとvalueを繋げる
            .collect(Collectors.joining("\n")); // それぞれを改行で連結する

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":bookmark_tabs: 現在のエイリアス")
            .setDescription(list)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void parseAlias(SlashCommandEvent event) {
        String orig_text = Main.getExistsOption(event, "text").getAsString();
        String text = orig_text;

        for (Map.Entry<String, String> entry : LibValue.aliasMap.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        event.replyEmbeds(new EmbedBuilder()
            .setDescription("`%s` -> `%s`".formatted(orig_text, text))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
