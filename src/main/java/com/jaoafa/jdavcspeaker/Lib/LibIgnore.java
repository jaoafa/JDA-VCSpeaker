package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONObject;

public class LibIgnore {
    private static final LibFiles.VFile vFile = LibFiles.VFile.IGNORE;

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

        LibValue.ignoreContains.clear();
        LibValue.ignoreEquals.clear();

        JSONObject obj = vFile.readJSONObject();
        if (obj == null) {
            ignoreFlow.error("除外ファイルの読み込みに失敗しました。");
            return;
        }

        for (int i = 0; i < obj.getJSONArray("contain").length(); i++) {
            LibValue.ignoreContains.add(obj.getJSONArray("contain").getString(i));
        }
        for (int i = 0; i < obj.getJSONArray("equal").length(); i++) {
            LibValue.ignoreEquals.add(obj.getJSONArray("equal").getString(i));
        }
        ignoreFlow.success("除外設定をロードしました（含む: %d / 一致: %d）。".formatted(LibValue.ignoreContains.size(), LibValue.ignoreEquals.size()));
    }

    public static void saveJson() {
        JSONObject obj = new JSONObject();
        obj.put("contain", LibValue.ignoreContains);
        obj.put("equal", LibValue.ignoreEquals);
        vFile.write(obj);
    }

    public static void addToContainIgnore(String value) {
        LibValue.ignoreContains.add(value);
        saveJson();
    }

    public static void addToEqualIgnore(String value) {
        LibValue.ignoreEquals.add(value);
        saveJson();
    }

    public static void removeToContainIgnore(String value) {
        LibValue.ignoreContains.remove(value);
        saveJson();
    }

    public static void removeToEqualIgnore(String value) {
        LibValue.ignoreEquals.remove(value);
        saveJson();
    }
}