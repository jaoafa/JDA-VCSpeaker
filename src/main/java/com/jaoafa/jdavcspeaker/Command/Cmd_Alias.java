package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibAlias;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Cmd_Alias implements CmdSubstrate {
    private final Pattern emojiPattern = Pattern.compile("^<:.+?:[0-9]+?>$");

    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":repeat:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "読みのエイリアスを設定します")
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
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        switch (subCmd) {
            case "add" -> addAlias(event, user);
            case "remove" -> removeAlias(event, user);
            case "list" -> listAlias(event, user);
            case "parse" -> parseAlias(event, user);
        }
    }

    void addAlias(SlashCommandInteractionEvent event, User user) {
        String from = Main.getExistsOption(event, "from").getAsString();
        String to = Main.getExistsOption(event, "to").getAsString();
        try {
            Pattern.compile(from);
        } catch (PatternSyntaxException e) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":no_entry_sign: 不正な正規表現です！")
                .setDescription("```\n" + e.getMessage() + "\n```")
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        }

        LibAlias.addToAlias(from, to);

        boolean isOnlyEmoji = emojiPattern.matcher(from).matches();
        String fromDisplay = isOnlyEmoji ? from : "`" + from + "`";

        cmdFlow.success("%s がエイリアスを設定しました: %s -> %s", event.getUser().getAsTag(), from, to);
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(":pencil: エイリアスを設定しました！")
            .addField(":repeat: 置き換え", "%s → `%s`".formatted(fromDisplay, to), false)
            .setAuthor(user.getAsTag(), "https://discord.com/users/%s".formatted(user.getId()), user.getEffectiveAvatarUrl())
            .setColor(LibEmbedColor.success);

        if (isOnlyEmoji) {
            builder.setFooter("Botが参加していないサーバのサーバ絵文字をエイリアス元としている場合、上手く表示されませんが正常に登録されています。");
        }

        event.replyEmbeds(builder.build()).queue();
    }

    void removeAlias(SlashCommandInteractionEvent event, User user) {
        String from = Main.getExistsOption(event, "from").getAsString();

        if (!LibAlias.getAliases().containsKey(from)) {
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

        String to = LibAlias.getAliasValue(from);
        LibAlias.removeFromAlias(from);
        cmdFlow.success("%s がエイリアスを削除しました: %s", event.getUser().getAsTag(), from);

        boolean isOnlyEmoji = emojiPattern.matcher(from).matches();
        String fromDisplay = isOnlyEmoji ? from : "`" + from + "`";

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wastebasket: エイリアスを削除しました！")
            .setDescription("次のエイリアスを削除: %s -> `%s`".formatted(fromDisplay, to))
            .setAuthor(user.getAsTag(), "https://discord.com/users/%s".formatted(user.getId()), user.getEffectiveAvatarUrl())
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void listAlias(SlashCommandInteractionEvent event, User user) {
        OptionMapping page_opt = event.getOption("page");
        int page = 1;
        if (page_opt != null) {
            page = Math.toIntExact(page_opt.getAsLong());
        }
        int indexPage = page - 1;

        List<String> list = LibAlias.getAliases().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String from = entry.getKey();
                boolean isOnlyEmoji = emojiPattern.matcher(from).matches();
                String fromDisplay = isOnlyEmoji ? from : "`" + from + "`";
                return "%s -> `%s`".formatted(fromDisplay, entry.getValue());
            }) // keyとvalueを繋げる
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
            .setDescription("Botが参加していないサーバのサーバ絵文字をエイリアス元としている場合、上手く表示されませんが正常に登録されています。\n\n" + String.join("\n", paginated.get(indexPage)))
            .setFooter("Page: " + page + " / " + paginated.size())
            .setAuthor(user.getAsTag(), "https://discord.com/users/%s".formatted(user.getId()), user.getEffectiveAvatarUrl())
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void parseAlias(SlashCommandInteractionEvent event, User user) {
        String orig_text = Main.getExistsOption(event, "text").getAsString();
        String text = orig_text;

        List<String> replaceAliases = new LinkedList<>();
        replaceAliases.add("`%s`".formatted(orig_text));
        for (Map.Entry<String, String> entry : LibAlias
            .getAliases()
            .entrySet()
            .stream()
            .sorted(Comparator.<Map.Entry<String, String>>comparingInt(e -> e.getKey().length()).reversed())
            .toList()) {
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                if (matcher.replaceAll(entry.getValue()).equals(text)) {
                    continue;
                }
                text = matcher.replaceAll(entry.getValue());
                text = text.replace(entry.getKey(), entry.getValue());
                replaceAliases.add("-> `%s`: `%s`".formatted(entry.getKey(), text));
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":eyes: エイリアスパース結果")
            .setAuthor(user.getAsTag(), "https://discord.com/users/%s".formatted(user.getId()), user.getEffectiveAvatarUrl())
            .setColor(LibEmbedColor.success);
        if (String.join("\n", replaceAliases).length() <= MessageEmbed.DESCRIPTION_MAX_LENGTH) {
            embed.setDescription(String.join("\n", replaceAliases));
        } else {
            embed.setDescription("`%s` -> `%s`".formatted(orig_text, String.join("\n", replaceAliases)));
        }

        event.replyEmbeds(embed.build()).queue();
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
            if (String.join("\n", temp).length() + s.length() >= 1900) {
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
