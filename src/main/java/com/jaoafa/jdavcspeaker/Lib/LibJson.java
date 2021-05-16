package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LibJson {
    public static void writeObject(String path, JSONObject jsonObject) {
        try {
            FileWriter file = new FileWriter(path);
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            pw.println(jsonObject);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeArray(String path, JSONArray jsonArray) {
        try {
            FileWriter file = new FileWriter(path);
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            pw.println(jsonArray);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readObject(String path) {
        final String[] jsonst = {""};
        try {
            Files.readAllLines(Paths.get(path)).forEach(s -> {
                jsonst[0] = jsonst[0] + s + "\n";
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject(jsonst[0]);
        return json;
    }

    public static JSONArray readArray(String path) {
        final String[] jsonst = {""};
        try {
            Files.readAllLines(Paths.get(path)).forEach(s -> {
                jsonst[0] = jsonst[0] + s + "\n";
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray json = new JSONArray(jsonst[0]);
        return json;
    }
}
