package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LibAlias {
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

        LibValue.aliasMap.clear();
        try {
            List<String> lines = Files.readAllLines(Paths.get("alias.json"));
            JSONObject obj = new JSONObject(String.join("\n", lines));

            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                String key = it.next();
                LibValue.aliasMap.put(key, obj.getString(key));
            }
            aliasFlow.success("エイリアス設定を " + LibValue.aliasMap.size() + " 件ロードしました。");
        } catch (IOException e) {
            aliasFlow.error("除外設定のロード中にエラーが発生しました。");
            e.printStackTrace();
        }
    }

    public static void fetchJson() {
        JSONObject obj = new JSONObject();
        LibValue.aliasMap.forEach(obj::put);
        try {
            Files.write(Paths.get("alias.json"), Collections.singleton(obj.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToAlias(String value1, String value2) {
        LibValue.aliasMap.put(value1, value2);
        fetchJson();
    }

    public static void removeFromAlias(String value) {
        LibValue.aliasMap.remove(value);
        fetchJson();
    }
}