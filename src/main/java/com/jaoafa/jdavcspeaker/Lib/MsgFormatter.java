package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.*;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MsgFormatter {
    static final Pattern parenthesesPattern = Pattern.compile("\\(.+\\)");

    public static String format(String text) {
        // ReplaceUnicodeEmoji
        text = EmojiWrapper.parseToAliases(text);

        // Alias
        text = LibAlias.applyAlias(text);

        // ReplaceCustomEmoji
        String regex = "<a?:(.+?):(\\d+)>";
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
            //noinspection HttpUrlsUsage
            if (s.startsWith("https://") || s.startsWith("http://")) {
                String[] urlSplit = s.split("/");
                text = text.replace(s, urlSplit[urlSplit.length - 1]);
            }
        }

        // LengthCheck
        if (text.length() >= 180) {
            text = text.substring(0, 180);
        }
        return text;
    }

    public static String formatChannelName(String channelName) {
        channelName = EmojiWrapper.removeAllEmojis(channelName); // 絵文字の削除
        channelName = parenthesesPattern.matcher(channelName).replaceAll(""); // かっこの削除
        return channelName;
    }

    public static String getDisplayContent(
        Message message,
        boolean replaceUserMentions,
        boolean replaceEmote,
        boolean replaceChannelMentions,
        boolean replaceRoleMentions
    ) {
        String content = message.getContentRaw();
        if (replaceUserMentions) {
            for (User user : message.getMentions().getUsers()) {
                String name;
                if (message.isFromGuild()) {
                    Member member = message.getGuild().getMember(user);
                    if (member != null) {
                        name = member.getEffectiveName();
                    } else {
                        name = user.getName();
                    }
                } else {
                    name = user.getName();
                }
                content = content.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
            }
        }

        if (replaceEmote) {
            for (Emote emote : message.getMentions().getEmotes()) {
                content = content.replace(emote.getAsMention(), ":" + emote.getName() + ":");
            }
        }

        if (replaceChannelMentions) {
            for (GuildChannel mentionedChannel : message.getMentions().getChannels()) {
                content = content.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
            }
        }

        if (replaceRoleMentions) {
            for (Role role : message.getMentions().getRoles()) {
                content = content.replace(role.getAsMention(), '@' + role.getName());
            }
        }

        return content;
    }
}
