package com.jaoafa.jdavcspeaker.Lib;


import org.json.JSONObject;

import java.io.IOException;

public class LibConfig {
    public static JSONObject config;

    public static void reload() {
        try {
            config = LibJson.readObject("./VCSpeaker.json").getJSONObject("yuuaTone");
        } catch (IOException e) {
            new LibReporter(null,e);
        }
    }
}
