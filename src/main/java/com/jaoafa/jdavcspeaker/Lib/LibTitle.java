package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.json.JSONObject;

public class LibTitle {
    public static JSONObject titleSetting = LibJson.readObject("./title.json");
    public static void saveSetting(JSONObject jsonObject){
        LibJson.writeObject(
                "./title.json",
                jsonObject
        );
        //同期
        titleSetting = LibJson.readObject("./title.json");
    }

    //現在のタイトルを変更します
    public static boolean setTitle(VoiceChannel channel,String name){
        //もし登録されてなかったら弾く
        if (!titleSetting.has(channel.getId())) {
            return false;
        }
        //名前変えて、設定ファイルにも記述
        channel.getManager().setName(name).queue();
        saveSetting(
                titleSetting.put(
                        channel.getId(),
                        titleSetting
                                .getJSONObject(channel.getId())
                                .put("current",name)
                                .put("modified",true)
                )
        );
        return true;
    }
    //もともとのタイトルを変更します
    public static boolean setOriginalTitle(VoiceChannel channel,String name){
        //もし登録されてなかったら弾く
        if (!titleSetting.has(channel.getId())) {
            return false;
        }
        //名前変えて、設定ファイルにも記述
        channel.getManager().setName(name).queue();
        saveSetting(
                titleSetting.put(
                        channel.getId(),
                        titleSetting
                                .getJSONObject(channel.getId())
                                .put("original",name)
                                .put("current","")
                                .put("modified",false)
                )
        );
        return true;
    }
    //元のタイトルに戻します
    public static boolean restoreTitle(VoiceChannel channel){
        //もし登録されてなかったら弾く
        if (!titleSetting.has(channel.getId())) {
            return false;
        }
        //名前を戻して設定ファイルに記述
        channel.getManager().setName(
                titleSetting.getJSONObject(channel.getId()).getString("original")
        ).queue();
        saveSetting(
                titleSetting.put(
                        channel.getId(),
                        titleSetting
                                .getJSONObject(channel.getId())
                                .put("current","")
                                .put("modified",false)
                )
        );
        return true;
    }
    //全VCをOriginalとして強制的に保存します。(上書き/modifiedがtrueの場合は除く)
    public static boolean saveAsOriginalAll(Guild guild){
        guild.getVoiceChannels().forEach(s -> {
            //profileが存在かつmodifiedがtrue
            if (titleSetting.has(s.getId())&&titleSetting.getJSONObject(s.getId()).getBoolean("modified")){
                return;
            }
            //profileが存在するかしないかに関わらず更新
            saveSetting(
                    titleSetting.put(
                            s.getId(),
                            titleSetting
                                    .getJSONObject(s.getId())
                                    .put("original",s.getName())
                                    .put("current","")
                                    .put("modified",false)
                    )
            );
        });
        return true;
    }
    //指定VCをOriginalとして強制的に保存します。(上書き)
    public static boolean saveAsOriginal(VoiceChannel channel){
        saveSetting(
                titleSetting.put(
                        channel.getId(),
                        titleSetting
                                .getJSONObject(channel.getId())
                                .put("original",channel.getName())
                                .put("current","")
                                .put("modified",false)
                )
        );
        return true;
    }
}
