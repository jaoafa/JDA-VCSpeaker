package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.StaticData;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

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

        StaticData.ignoreContains.clear();
        StaticData.ignoreEquals.clear();

        try {
            List<String> lines = Files.readAllLines(Paths.get("ignore.json"));
            JSONObject obj = new JSONObject(String.join("\n", lines));

            for (int i = 0; i < obj.getJSONArray("contain").length(); i++) {
                StaticData.ignoreContains.add(obj.getJSONArray("contain").getString(i));
            }
            for (int i = 0; i < obj.getJSONArray("equal").length(); i++) {
                StaticData.ignoreEquals.add(obj.getJSONArray("equal").getString(i));
            }
            ignoreFlow.success("除外設定をロードしました（含む: %d / 一致: %d）。".formatted(StaticData.ignoreContains.size(), StaticData.ignoreEquals.size()));
        } catch (IOException e) {
            ignoreFlow.error("除外設定のロード中にエラーが発生しました。");
            new LibReporter(null, e);
            e.printStackTrace();
        }
    }

    public static void saveJson() {
        JSONObject obj = new JSONObject();
        obj.put("contain", StaticData.ignoreContains);
        obj.put("equal", StaticData.ignoreEquals);
        try {
            Files.write(Paths.get("ignore.json"), Collections.singleton(obj.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToContainIgnore(String value) {
        StaticData.ignoreContains.add(value);
        saveJson();
    }

    public static void addToEqualIgnore(String value) {
        StaticData.ignoreEquals.add(value);
        saveJson();
    }

    public static void removeToContainIgnore(String value) {
        StaticData.ignoreContains.remove(value);
        saveJson();
    }

    public static void removeToEqualIgnore(String value) {
        StaticData.ignoreEquals.remove(value);
        saveJson();
    }
}