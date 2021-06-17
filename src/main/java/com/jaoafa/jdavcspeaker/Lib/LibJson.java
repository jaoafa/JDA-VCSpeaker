package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONException;
import org.json.JSONObject;

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
     * @param _path JSONファイルへのパス
     *
     * @return JSONオブジェクト
     *
     * @throws IOException          入出力例外が発生した場合
     * @throws JSONException        JSONとして正しくないなど、パースできなかった場合
     * @throws InvalidPathException パス文字列をPathに変換できない場合
     */
    public static JSONObject readObject(String _path) throws IOException, JSONException, InvalidPathException {
        Path path = Paths.get(_path);
        JSONObject object = new JSONObject();
        if (!Files.exists(path)) {
            return object;
        }
        String json = String.join("\n", Files.readAllLines(path, Charset.defaultCharset()));
        return new JSONObject(json);
    }

    /**
     * JSONファイルを配列として読み出します。
     *
     * @param _path JSONファイルへのパス
     * @return JSON配列
     * @throws IOException          入出力例外が発生した場合
     * @throws JSONException        JSONとして正しくないなど、パースできなかった場合
     * @throws InvalidPathException パス文字列をPathに変換できない場合
     */
    /*
    public static JSONArray readArray(String _path) throws IOException, JSONException, InvalidPathException {
        Path path = Paths.get(_path);
        JSONArray array = new JSONArray();
        if (!Files.exists(path)) {
            return array;
        }
        String json = String.join("\n", Files.readAllLines(path, Charset.defaultCharset()));
        return new JSONArray(json);
    }
    */

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
     * 配列をJSONファイルに書き出します。
     *
     * @param path  書き出すJSONファイルのパス
     * @param array 書き出す配列
     * @throws IOException          入出力例外が発生した場合
     * @throws InvalidPathException パス文字列をPathに変換できない場合
     */
    /*
    public static void writeArray(String path, JSONArray array) throws IOException, InvalidPathException {
        Files.write(Paths.get(path), Collections.singleton(array.toString()));
    }
    */
}
