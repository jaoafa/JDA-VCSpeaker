package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LibIgnore {
    private static final LibFiles.VFile vFile = LibFiles.VFile.IGNORE;
    public static final List<String> contains = new ArrayList<>();
    public static final List<String> equals = new ArrayList<>();
    public static final List<String> regexs = new ArrayList<>();

    public static void fetchMap() {
        LibFlow ignoreFlow = new LibFlow("LibIgnore");
        //存在しない場合に作成
        if (!vFile.exists()) {
            boolean bool = vFile.write(new JSONObject());
            if (bool) {
                ignoreFlow.success("除外ファイルの作成に成功しました。");
            } else {
                ignoreFlow.error("除外ファイルの作成に失敗しました。");
            }
        }

        contains.clear();
        equals.clear();
        regexs.clear();

        JSONObject obj = vFile.readJSONObject();
        if (obj == null) {
            ignoreFlow.error("除外ファイルの読み込みに失敗しました。");
            return;
        }

        for (int i = 0; i < obj.getJSONArray("contain").length(); i++) {
            contains.add(obj.getJSONArray("contain").getString(i));
        }
        for (int i = 0; i < obj.getJSONArray("equal").length(); i++) {
            equals.add(obj.getJSONArray("equal").getString(i));
        }
        for (int i = 0; i < obj.getJSONArray("regex").length(); i++) {
            regexs.add(obj.getJSONArray("regex").getString(i));
        }
        ignoreFlow.success("除外設定をロードしました（含む: %d / 一致: %d / 正規表現: %d）。".formatted(contains.size(), equals.size(), regexs.size()));
    }

    public static void saveJson() {
        JSONObject obj = new JSONObject();
        obj.put("contain", contains);
        obj.put("equal", equals);
        obj.put("regex", regexs);
        vFile.write(obj);
    }

    public static void addToContainIgnore(String value) {
        contains.add(value);
        saveJson();
    }

    public static void addToEqualIgnore(String value) {
        equals.add(value);
        saveJson();
    }

    public static void addToRegexIgnore(String regex) {
        try {
            Pattern.compile(regex);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid regex: " + regex);
        }
        regexs.add(regex);
        saveJson();
    }

    public static void removeToContainIgnore(String value) {
        contains.remove(value);
        saveJson();
    }

    public static void removeToEqualIgnore(String value) {
        equals.remove(value);
        saveJson();
    }

    public static void removeToRegexIgnore(String regex) {
        regexs.remove(regex);
        saveJson();
    }

    public static boolean isIgnoreMessage(String content) {
        boolean isEquals = equals.contains(content);
        boolean isContain = contains.stream().anyMatch(content::contains);
        boolean isRegex = regexs.stream().anyMatch(regex -> Pattern.compile(regex).matcher(content).find());

        return isEquals || isContain || isRegex;
    }
}