package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LibAlias {
    private static final LibFlow aliasFlow = new LibFlow("LibAlias");
    private static final Map<String, String> aliases = new HashMap<>();

    public static void fetchMap() {
        if (!LibFiles.VFile.ALIAS.exists()) {
            boolean bool = LibFiles.VFile.ALIAS.write(new JSONObject());
            if (bool) {
                aliasFlow.success("エイリアスファイルの作成に成功しました。");
            } else {
                aliasFlow.error("エイリアスファイルの作成に失敗しました。");
            }
        }

        aliases.clear();
        JSONObject obj = LibFiles.VFile.ALIAS.readJSONObject(new JSONObject());

        for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
            String key = it.next();
            aliases.put(key, obj.getString(key));
        }
        aliasFlow.success("エイリアス設定を " + aliases.size() + " 件ロードしました。");
    }

    public static void fetchJson() {
        JSONObject obj = new JSONObject();
        aliases.forEach(obj::put);
        boolean bool = LibFiles.VFile.ALIAS.write(obj);
        if (!bool) {
            aliasFlow.error("エイリアスファイルの書き込みに失敗しました。");
        }
    }

    public static void addToAlias(String key, String value) {
        aliases.put(key, value);
        fetchJson();
    }

    public static void removeFromAlias(String key) {
        aliases.remove(key);
        fetchJson();
    }

    public static String getAliasValue(String key) {
        return aliases.get(key);
    }

    public static Map<String, String> getAliases() {
        return aliases;
    }

    public static String applyAlias(String text) {
        for (Map.Entry<String, String> entry : aliases
            .entrySet()
            .stream()
            .sorted(Comparator.<Map.Entry<String, String>>comparingInt(e -> e.getKey().length()).reversed())
            .toList()) {
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                text = matcher.replaceAll(entry.getValue());
            }
        }
        return text;
    }
}