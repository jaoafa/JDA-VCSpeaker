package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class MultipleServer {
    private static final File file = new File("servers.json");

    /**
     * VCSpeakerの対象サーバに追加します。
     *
     * @param guild   追加するサーバ
     * @param channel サーバのVCチャンネル
     *
     * @return 追加できたかどうか
     */
    public static boolean addServer(Guild guild, MessageChannel channel) {
        JSONObject data = getData();
        JSONObject servers = data.optJSONObject("servers");
        if (servers == null) servers = new JSONObject();
        servers.put(guild.getId(), channel.getIdLong());
        data.put("servers", servers);
        return saveData(data);
    }

    /**
     * VCSpeakerの対象サーバから削除します。
     *
     * @param guild 削除するサーバ
     *
     * @return 削除できたかどうか
     *
     * @see #isTargetServer(Guild)
     */
    public static boolean removeServer(Guild guild) {
        JSONObject data = getData();
        JSONObject servers = data.optJSONObject("servers");
        if (servers == null) servers = new JSONObject();
        servers.remove(guild.getId());
        data.put("servers", servers);
        return saveData(data);
    }

    /**
     * VCSpeakerの対象サーバであるかどうかを返します
     *
     * @param guild 確認するサーバ
     *
     * @return 対象であるかどうか
     *
     * @see #addServer(Guild, MessageChannel)
     * @see #removeServer(Guild)
     */
    public static boolean isTargetServer(Guild guild) {
        return getServers().has(guild.getId());
    }

    /**
     * サーバのvCチャンネルを返します
     *
     * @param guild サーバ
     *
     * @return 設定されているVCチャンネル
     *
     * @see #isTargetServer(Guild)
     */
    public static TextChannel getVCChannel(Guild guild) {
        return StaticData.jda.getTextChannelById(getVCChannelId(guild));
    }

    /**
     * サーバのVCチャンネルのIDを返します
     *
     * @param guild サーバ
     *
     * @return 設定されているVCチャンネルのID
     *
     * @see #isTargetServer(Guild)
     */
    public static long getVCChannelId(Guild guild) {
        return getServers().getLong(guild.getId());
    }

    /**
     * 会話開始時の通知チャンネルを設定します
     *
     * @param guild   設定するサーバ
     * @param channel 通知先として設定するチャンネル
     *
     * @return 設定できたかどうか
     */
    public static boolean setNotifyChannel(Guild guild, MessageChannel channel) {
        JSONObject data = getData();
        JSONObject notifies = data.optJSONObject("notifies");
        if (notifies == null) notifies = new JSONObject();
        notifies.put(guild.getId(), channel.getIdLong());
        data.put("notifies", notifies);
        return saveData(data);
    }

    /**
     * サーバから通知チャンネル設定を削除します
     *
     * @param guild 通知チャンネル設定を削除するサーバ
     *
     * @return 削除できたかどうか
     *
     * @see #isNotifiable(Guild)
     */
    public static boolean removeNotifyChannel(Guild guild) {
        JSONObject data = getData();
        JSONObject notifies = data.optJSONObject("notifies");
        if (notifies == null) notifies = new JSONObject();
        notifies.remove(guild.getId());
        data.put("notifies", notifies);
        return saveData(data);
    }

    /**
     * サーバに通知チャンネル設定がされているかどうかを返します
     *
     * @param guild サーバ
     *
     * @return 通知チャンネル設定がされているかどうか
     *
     * @see #setNotifyChannel(Guild, MessageChannel)
     */
    public static boolean isNotifiable(Guild guild) {
        return getNotifies().has(guild.getId());
    }

    /**
     * 通知チャンネルを返します
     *
     * @param guild サーバ
     *
     * @return サーバの通知チャンネル
     */
    public static TextChannel getNotifyChannel(Guild guild) {
        return StaticData.jda.getTextChannelById(getNotifyChannelId(guild));
    }

    /**
     * 通知チャンネルのIDを返します
     *
     * @param guild サーバ
     *
     * @return サーバの通知チャンネルのID
     */
    public static long getNotifyChannelId(Guild guild) {
        return getNotifies().getLong(guild.getId());
    }

    private static JSONObject getData() {
        if (!file.exists()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(String.join("\n", Files.readAllLines(file.toPath())));
        } catch (IOException e) {
            return new JSONObject();
        }
    }

    private static JSONObject getServers() {
        JSONObject data = getData();
        JSONObject servers = data.optJSONObject("servers");
        if (servers == null) servers = new JSONObject();
        return servers;
    }

    private static JSONObject getNotifies() {
        JSONObject data = getData();
        JSONObject notifies = data.optJSONObject("notifies");
        if (notifies == null) notifies = new JSONObject();
        return notifies;
    }

    private static boolean saveData(JSONObject object) {
        try {
            Files.write(file.toPath(), Collections.singleton(object.toString()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
