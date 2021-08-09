package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class LibTitle {
    @Nullable
    public JSONObject titleSetting = new JSONObject();

    public LibTitle(String path) throws Exception {
        if (!new File(path).exists()) {
            return;
        }
        titleSetting = LibJson.readObject(path);
    }

    /**
     * VCタイトル設定が存在するかを調べます
     *
     * @param channel 対象チャンネル
     *
     * @return 存在するか
     */
    public boolean existsTitle(VoiceChannel channel) {
        if (titleSetting == null) return false;
        return titleSetting.has(channel.getId());
    }

    /**
     * VCタイトル設定が存在し、かつ変更済みかどうかを調べます
     *
     * @param channel 対象チャンネル
     *
     * @return 存在するか
     */
    public boolean isModifiedTitle(VoiceChannel channel) {
        if (titleSetting == null) return false;
        if (!titleSetting.has(channel.getId())) return false;
        return titleSetting.getJSONObject(channel.getId()).getBoolean("modified");
    }

    /**
     * 現在のVC名と設定で保持しているVC名が同じかどうか調べます
     *
     * @param channel 対象チャンネル
     *
     * @return 同じかどうか
     */
    public Boolean checkTitleIsSame(VoiceChannel channel) {
        if (titleSetting == null) return false;
        if (!titleSetting.has(channel.getId())) return false;
        String original_title = getOriginalTitle(channel);
        if (original_title == null) return false;
        //Title使用中だったら中止
        if (isModifiedTitle(channel)) return null;

        return original_title.equals(channel.getName());
    }

    /**
     * 現在の設定中VCタイトルを取得します
     *
     * @param channel 対象チャンネル
     *
     * @return 設定中のVCタイトル
     */
    @Nullable
    public String getCurrentTitle(VoiceChannel channel) {
        if (titleSetting == null) return null;
        if (!titleSetting.has(channel.getId())) return null;
        return titleSetting.getJSONObject(channel.getId()).getString("current");
    }

    /**
     * 現在の設定中オリジナルVCタイトルを取得します
     *
     * @param channel 対象チャンネル
     *
     * @return 設定中のオリジナルVCタイトル
     */
    @Nullable
    public String getOriginalTitle(VoiceChannel channel) {
        if (titleSetting == null) return null;
        if (!titleSetting.has(channel.getId())) return null;
        return titleSetting.getJSONObject(channel.getId()).getString("original");
    }

    /**
     * 現在のVCタイトルを変更します
     *
     * @param channel 対象チャンネル
     * @param name    変更後のタイトル
     *
     * @return 成功したか
     */
    public boolean setTitle(VoiceChannel channel, String name) {
        if (titleSetting == null) return false;
        // もし登録されてなかったら弾く
        if (!titleSetting.has(channel.getId())) {
            return false;
        }
        //もし手動で変更されていたら(設定ファイルと違ったら)
        //変更する前にオリジナルとして保存
        if (checkTitleIsSame(channel) != null && !checkTitleIsSame(channel)) {
            saveSetting(
                titleSetting.put(
                    channel.getId(),
                    new JSONObject()
                        .put("original", channel.getName())
                )
            );
        }
        // 名前変えて、設定ファイルにも記述
        channel.getManager().setName(name).queue();
        saveSetting(
            titleSetting.put(
                channel.getId(),
                titleSetting
                    .getJSONObject(channel.getId())
                    .put("current", name)
                    .put("modified", true)
            )
        );
        return true;
    }

    /**
     * オリジナルVCタイトルに戻します
     *
     * @param channel 対象チャンネル
     */
    public void restoreTitle(VoiceChannel channel) {
        if (titleSetting == null) return;
        //もし登録されてなかったら弾く
        if (!titleSetting.has(channel.getId())) {
            return;
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
                    .put("current", "")
                    .put("modified", false)
            )
        );
    }

    /**
     * 全VCをOriginalとして強制的に保存します。(上書き/modifiedがtrueの場合は除く)
     *
     * @param guild 対象サーバ
     *
     * @return 成功したか
     */
    public boolean saveAsOriginalAll(Guild guild) {
        if (titleSetting == null) return false;
        guild.getVoiceChannels().forEach(s -> {
            // profileが存在かつmodifiedがtrue
            if (titleSetting.has(s.getId()) && titleSetting.getJSONObject(s.getId()).getBoolean("modified")) {
                return;
            }
            // profileが存在するかしないかに関わらず更新
            saveSetting(
                titleSetting.put(
                    s.getId(),
                    new JSONObject()
                        .put("original", s.getName())
                        .put("current", "")
                        .put("modified", false)
                )
            );
        });
        return true;
    }

    /**
     * 指定VCをOriginalとして強制的に保存します。(上書き)
     *
     * @param channel 対象チャンネル
     *
     * @return 成功したか
     */
    public boolean saveAsOriginal(VoiceChannel channel) {
        if (titleSetting == null) return false;
        if (titleSetting.has(channel.getId())) {
            saveSetting(
                titleSetting.put(
                    channel.getId(),
                    new JSONObject()
                        .put("original", channel.getName())
                        .put("current", "")
                        .put("modified", false)
                )
            );
        }
        return true;
    }

    /**
     * 退出時、タイトルを戻す必要があるかどうかを調べ、その必要があれば戻します。
     *
     * @param vc 対象のチャンネル
     */
    public void processLeftTitle(VoiceChannel vc) {
        long nonBotUsers = vc
            .getMembers()
            .stream()
            .filter(member -> !member.getUser().isBot())
            .count();
        if (nonBotUsers != 0) {
            return;
        }
        if (!isModifiedTitle(vc)) {
            return;
        }
        restoreTitle(vc);
    }

    void saveSetting(JSONObject object) {
        titleSetting = object;
        try {
            LibJson.writeObject("./title.json", object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
