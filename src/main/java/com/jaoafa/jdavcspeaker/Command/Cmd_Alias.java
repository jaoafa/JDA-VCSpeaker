package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibAlias;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
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
                        new SubcommandData("list", "エイリアス一覧を表示します")
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        switch (subCmd) {
            case "add" -> addAlias(event);
            case "remove" -> removeAlias(event);
            case "list" -> listAlias(event);
        }
    }

    void addAlias(SlashCommandEvent event) {
        String from = event.getOption("from").getAsString(/*絶対100%確実にRequired*/);
        String to = event.getOption("to").getAsString(/*絶対100%確実にRequired*/);

        LibAlias.addToAlias(from, to);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":pencil: エイリアスを設定しました！")
            .setDescription("`%s`を`%s`に置き換えて読み上げます。".formatted(from, to))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeAlias(SlashCommandEvent event) {
        String from = event.getOption("from").getAsString(/*絶対100%確実にRequired*/);

        if (!StaticData.aliasMap.containsKey(from)) {
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

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: エイリアスを削除しました！")
            .setDescription("`%s`の置き換えを削除しました。".formatted(from))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void listAlias(SlashCommandEvent event) {
        String list = StaticData.aliasMap.entrySet().stream()
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
}
