package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class LibJson {
    /**
     * JSONファイルをオブジェクトとして読み出します。
     *
     * @param file JSONファイルへのFile
     *
     * @return JSONオブジェクト
     *
     * @throws IOException          入出力例外が発生した場合
     * @throws JSONException        JSONとして正しくないなど、パースできなかった場合
     * @throws InvalidPathException パス文字列をPathに変換できない場合
     */
    public static JSONObject readObject(File file) throws IOException, JSONException, InvalidPathException {
        return readObject(file.toPath());
    }

    /**
     * JSONファイルをオブジェクトとして読み出します。
     *
     * @param path JSONファイルのPath
     *
     * @return JSONオブジェクト
     *
     * @throws IOException          入出力例外が発生した場合
     * @throws JSONException        JSONとして正しくないなど、パースできなかった場合
     * @throws InvalidPathException パス文字列をPathに変換できない場合
     */
    public static JSONObject readObject(Path path) throws IOException, JSONException, InvalidPathException {
        JSONObject object = new JSONObject();
        if (!Files.exists(path)) {
            return object;
        }
        String json = String.join("\n", Files.readAllLines(path, Charset.defaultCharset()));
        return new JSONObject(json);
    }

    /**
     * オブジェクトをJSONファイルに書き出します。
     *
     * @param path   書き出すJSONファイルのパス
     * @param object 書き出すオブジェクト
     *
     * @throws IOException          入出力例外が発生した場合
     * @throws InvalidPathException パス文字列をPathに変換できない場合
     */
    public static void writeObject(String path, JSONObject object) throws IOException, InvalidPathException {
        Files.write(Paths.get(path), Collections.singleton(object.toString()));
    }

    /**
     * JSONObjectのString値をパスで検索します。
     *
     * @param object 対象のJSONObject
     * @param path   String値へのパス (yabai.json.powa)
     *
     * @return String値
     */
    public static String getByPath(JSONObject object, String path) {
        int currentPathCount = 1;
        String resultString = "";
        JSONObject currentObject = object;
        String[] pathString = path.split("\\.");
        for (String s : pathString) {
            if (pathString.length == currentPathCount) {
                resultString = currentObject.getString(s);
            } else {
                currentObject = currentObject.getJSONObject(s);
            }
            currentPathCount++;
        }
        return resultString;
    }
}
