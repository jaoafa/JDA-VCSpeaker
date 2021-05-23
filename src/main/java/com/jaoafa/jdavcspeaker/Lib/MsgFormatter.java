package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.StaticData;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MsgFormatter {
    static final Pattern parenthesesPattern = Pattern.compile("\\(.+\\)");

    public static String format(String text) {
        // LengthCheck
        if (text.length() >= 180) {
            text = text.substring(0, 180);
        }

        // ReplaceUnicodeEmoji
        text = EmojiParser.parseToAliases(text);

        // ReplaceCustomEmoji
        String regex = "<a?:(.+?):([0-9]+)>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            text = text.replace(m.group(), ":" + m.group(1) + ":");
        }

        // RemoveParams
        text = Arrays
            .stream(text.split(" "))
            .filter(s -> !s.startsWith("speaker:"))
            .filter(s -> !s.startsWith("speed:"))
            .filter(s -> !s.startsWith("emotion:"))
            .filter(s -> !s.startsWith("emotion_level:"))
            .filter(s -> !s.startsWith("pitch:"))
            .collect(Collectors.joining(" "));

        String[] splitText = text.split(" ");

        // URLCheck
        for (String s : splitText) {
            if (s.startsWith("https://") || s.startsWith("http://")) {
                String[] urlSplit = s.split("/");
                text = text.replace(s, urlSplit[urlSplit.length - 1]);
            }
        }

        // Alias
        for (Map.Entry<String, String> entry : StaticData.aliasMap.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    public static String formatChannelName(VoiceChannel channel) {
        String channelName = channel.getName();
        channelName = EmojiParser.removeAllEmojis(channelName); // 絵文字の削除
        channelName = parenthesesPattern.matcher(channelName).replaceAll(""); // かっこの削除
        return channelName;
    }
}
