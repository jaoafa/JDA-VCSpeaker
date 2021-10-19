package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class LibIgnore {
    public static void fetchMap() {
        LibFlow ignoreFlow = new LibFlow("LibIgnore");
        File ignoreConfig = new File("./ignore.json");
        //存在しない場合に作成
        if (!ignoreConfig.exists()) {
            try {
                Files.write(Paths.get("ignore.json"), Collections.singleton(new JSONObject().toString()));
                ignoreFlow.success("ignore.json ファイルの作成に成功しました。");
            } catch (IOException e) {
                ignoreFlow.error("ignore.json ファイルの作成に失敗しました。");
                e.printStackTrace();
            }
        }

        LibValue.ignoreContains.clear();
        LibValue.ignoreEquals.clear();

        try {
            JSONObject obj = new JSONObject(Files.readString(Paths.get("ignore.json")));

            for (int i = 0; i < obj.getJSONArray("contain").length(); i++) {
                LibValue.ignoreContains.add(obj.getJSONArray("contain").getString(i));
            }
            for (int i = 0; i < obj.getJSONArray("equal").length(); i++) {
                LibValue.ignoreEquals.add(obj.getJSONArray("equal").getString(i));
            }
            ignoreFlow.success("除外設定をロードしました（含む: %d / 一致: %d）。".formatted(LibValue.ignoreContains.size(), LibValue.ignoreEquals.size()));
        } catch (IOException e) {
            ignoreFlow.error("除外設定のロード中にエラーが発生しました。");
            new LibReporter(null, e);
            e.printStackTrace();
        }
    }

    public static void saveJson() {
        JSONObject obj = new JSONObject();
        obj.put("contain", LibValue.ignoreContains);
        obj.put("equal", LibValue.ignoreEquals);
        try {
            Files.write(Paths.get("ignore.json"), Collections.singleton(obj.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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