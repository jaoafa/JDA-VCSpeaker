package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LibAlias {
    private static final Map<String, String> aliases = new HashMap<>();

    public static void fetchMap() {
        LibFlow aliasFlow = new LibFlow("LibAlias");
        File aliasConfig = new File("./alias.json");
        //存在しない場合に作成
        if (!aliasConfig.exists()) {
            try {
                Files.write(Paths.get("alias.json"), Collections.singleton(new JSONObject().toString()));
                aliasFlow.success("alias.json ファイルの作成に成功しました。");
            } catch (IOException e) {
                aliasFlow.error("alias.json ファイルの作成に失敗しました。");
                e.printStackTrace();
            }
        }

        aliases.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get("alias.json"));
            JSONObject obj = new JSONObject(String.join("\n", lines));

            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                String key = it.next();
                aliases.put(key, obj.getString(key));
            }
            aliasFlow.success("エイリアス設定を " + aliases.size() + " 件ロードしました。");
        } catch (IOException e) {
            aliasFlow.error("エイリアス設定のロード中にエラーが発生しました。");
            e.printStackTrace();
        }
    }

    public static void fetchJson() {
        JSONObject obj = new JSONObject();
        aliases.forEach(obj::put);
        try {
            Files.write(Paths.get("alias.json"), Collections.singleton(obj.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToAlias(String value1, String value2) {
        aliases.put(value1, value2);
        fetchJson();
    }

    public static void removeFromAlias(String value) {
        aliases.remove(value);
        fetchJson();
    }

    public static Map<String, String> getAliases() {
        return aliases;
    }

    public static String applyAlias(String text) {
        for (Map.Entry<String, String> entry : aliases.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey().length())).collect(Collectors.toList())) {
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                text = matcher.replaceAll(entry.getValue());
            }
        }
        return text;
    }
}