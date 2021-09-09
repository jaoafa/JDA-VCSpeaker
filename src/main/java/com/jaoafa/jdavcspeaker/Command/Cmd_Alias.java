package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibAlias;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibValue;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.LinkedList;
import java.util.List;
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
                            .addOption(OptionType.INTEGER, "page", "表示するページ番号"),
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
            case "remove" -> removeAlias(event, user);
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
            .addField(":repeat: エイリアス", "`%s` → `%s`".formatted(from, to), false)
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void removeAlias(SlashCommandEvent event,User user) {
        String from = Main.getExistsOption(event, "from").getAsString();

        if (!LibValue.aliasMap.containsKey(from)) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":mag_right: エイリアスが見つかりませんでした！")
                .setDescription("""
                    `%s` に一致するエイリアスが見つかりませんでした。
                    `/alias list` で現在のエイリアスを確認することが出来ます。
                    """.formatted(from))
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        LibAlias.removeFromAlias(from);
        cmdFlow.success("%s がエイリアスを削除しました: %s", event.getUser().getAsTag(), from);

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: エイリアスを削除しました！")
            .setDescription("`%s` のエイリアスが %s により削除されました。".formatted(from, user.getAsMention()))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void listAlias(SlashCommandEvent event) {
        OptionMapping page_opt = event.getOption("page");
        int page = 1;
        if (page_opt != null) {
            page = Math.toIntExact(page_opt.getAsLong());
        }
        int indexPage = page - 1;

        List<String> list = LibValue.aliasMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> "`%s` -> `%s`".formatted(entry.getKey(), entry.getValue())) // keyとvalueを繋げる
            .collect(Collectors.toList());
        LinkedList<String> paginated = split2000(list);
        if (indexPage < 0 || indexPage >= paginated.size()) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: エイリアスの一覧出力に失敗しました。")
                .setDescription("エイリアスページ範囲外です。ページ番号には 1 ～ " + paginated.size() + " を指定できます。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":bookmark_tabs: 現在のエイリアス")
            .setDescription(String.join("\n", paginated.get(indexPage)))
            .setFooter("Page: " + page + " / " + paginated.size())
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

    /**
     * 2000文字を超えないようにsplitする
     *
     * @param strings 改行で分離させたList
     *
     * @return 1項目が2000文字を超えないテキストList
     */
    LinkedList<String> split2000(List<String> strings) {
        List<String> temp = new LinkedList<>();
        LinkedList<String> ret = new LinkedList<>();
        for (String s : strings) {
            if (String.join("\n", temp).length() + s.length() >= 2000) {
                ret.add(String.join("\n", temp));
                temp.clear();
            }
            temp.add(s);
        }
        if (!temp.isEmpty()) {
            ret.add(String.join("\n", temp));
        }
        return ret;
    }
}
