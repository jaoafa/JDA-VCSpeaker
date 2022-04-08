package com.jaoafa.jdavcspeaker.Lib;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmojiWrapper {
    /**
     * 文字列中のUnicode絵文字を:ALIAS:形式に変換する
     *
     * @param input 対象の文字列
     *
     * @return 変換後の文字列
     */
    public static @NotNull String parseToAliases(@NotNull String input) {
        List<String> rawEmojis = EmojiParser.extractEmojis(input);
        for (Emoji emoji : rawEmojis.stream().map(EmojiManager::getByUnicode).toList()) {
            String alias = ":" + emoji.getAliases().get(0) + ":";
            input = input.replaceAll(emoji.getUnicode(), alias);
            input = input.replaceAll(emoji.getTrimmedUnicode(), alias);
        }
        return input;
    }

    /**
     * 文字列中の絵文字を削除する
     *
     * @param input 対象の文字列
     *
     * @return 削除後の文字列
     */
    public static @NotNull String removeAllEmojis(@NotNull String input) {
        List<String> rawEmojis = EmojiParser.extractEmojis(input);
        for (Emoji emoji : rawEmojis.stream().map(EmojiManager::getByUnicode).toList()) {
            input = input.replaceAll(emoji.getUnicode(), "");
            input = input.replaceAll(emoji.getTrimmedUnicode(), "");
        }
        return input;
    }
}
