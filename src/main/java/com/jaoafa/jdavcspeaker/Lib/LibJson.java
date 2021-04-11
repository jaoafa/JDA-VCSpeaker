package com.jaoafa.jdavcspeaker.Lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class LibJson {
    public static void write(String path, JSONObject jsonObject) {
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
        String jsonst = null;
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str = br.readLine();
            while (str != null) {
                if (jsonst == null) {
                    jsonst = str;
                } else {
                    jsonst = jsonst + str;
                }
                str = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        JSONObject json = new JSONObject(jsonst);
        return json;
    }

    public static JSONArray readArray(String path) {
        String jsonst = null;
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str = br.readLine();
            while (str != null) {
                if (jsonst == null) {
                    jsonst = str;
                } else {
                    jsonst = jsonst + str;
                }
                str = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        JSONArray json = new JSONArray(jsonst);
        return json;
    }
}
