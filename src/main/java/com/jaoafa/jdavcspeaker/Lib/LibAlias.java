package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.StaticData;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;

public class LibAlias {
    public static void fetchMap(){
        File aliasConfig = new File("./alias.json");
        //存在しない場合に作成
        if (!aliasConfig.exists()){
            try {
                aliasConfig.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //:でsplitするのでalias追加するときに:が入らないように対策必要
        LibJson.readArray("./alias.json").forEach(s ->{
            StaticData.aliasMap.clear();
            String[] alias = s.toString().split(":");
            StaticData.aliasMap.put(alias[0],alias[1]);
        });
    }

    public static void fetchJson(){
        JSONArray fetchedJson = new JSONArray();
        StaticData.aliasMap.forEach((k,v) ->{
            fetchedJson.put(String.format("%s:%s",k,v));
        });
        LibJson.writeArray("./alias.json",fetchedJson);
    }

    public static void addToAlias(String value1,String value2){
        JSONArray addedJson = LibJson.readArray("./alias.json").put(String.format("%s:%s",value1,value2));
        LibJson.writeArray("./alias.json",addedJson);
        fetchMap();
    }

    public static void removeFromAlias(String value){
        StaticData.aliasMap.remove(value);
        fetchJson();
    }
}
