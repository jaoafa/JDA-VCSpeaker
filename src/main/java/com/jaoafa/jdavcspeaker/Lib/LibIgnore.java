package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.StaticData;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LibIgnore {
    public static void fetchMap() {
        File ignoreConfig = new File("./ignore.json");
        //存在しない場合に作成
        if (!ignoreConfig.exists()) {
            try {
                Files.write(Paths.get("ignore.json"), Collections.singleton(new JSONObject().toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get("ignore.json"));
            JSONObject obj = new JSONObject(String.join("\n", lines));

            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                String key = it.next();
                StaticData.ignoreMap.put(key, obj.getString(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveJson() {
        JSONObject obj = new JSONObject();
        StaticData.ignoreMap.forEach(obj::put);
        try {
            Files.write(Paths.get("ignore.json"), Collections.singleton(obj.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToIgnore(String value1, String value2) {
        StaticData.ignoreMap.put(value1, value2);
        saveJson();
    }

    public static void removeFromIgnore(String value1, String value2) {
        StaticData.ignoreMap.remove(value1, value2);
        saveJson();
    }
}