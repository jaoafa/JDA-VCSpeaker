package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.StaticData;
import com.vdurmont.emoji.EmojiParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgFormatter {
    public static String format(String text) {
        //LengthCheck
        if (text.length() >= 180) {
            text = text.substring(0, 180);
        }
        //EmojiCheck
        text = EmojiParser.parseToAliases(text);

        //CustomEmojiCheck
        String regex = "<a?:(.+?):([0-9]+)>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            text = text.replace(m.group(), ":" + m.group(1) + ":");
        }

        String[] splitedText = text.split(" ");

        //URLCheck
        for (String s : splitedText) {
            if (s.startsWith("https://") || s.startsWith("http://")) {
                String[] urlSplit = s.split("/");
                text = text.replace(s, urlSplit[urlSplit.length - 1]);
            }
        }

        final String[] formatText = {text};
        StaticData.aliasMap.forEach((k,v) ->{
            formatText[0] = formatText[0].replace(k,v);
        });
        return formatText[0];
    }
}
