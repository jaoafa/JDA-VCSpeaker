package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.entities.Member;
import com.jaoafa.jdavcspeaker.Lib.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

import java.text.MessageFormat;

/**
 * When someone leaves the VC, notify the VC text channel.
 */
public class Event_Disconnect extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        VoiceChannel vc = event.getChannelLeft();
        JSONObject titleSetting = LibJson.readObject("./title.json");
        //残りメンバー0人かつ登録されている場合
        //残りメンバー1人(VCSpeaker)かつ登録されている場合
        if (
                (vc.getMembers().size() == 0&&titleSetting.has(vc.getId()))||
                        (vc.getMembers().size() == 1&&vc.getMembers().contains((Member) StaticData.jda.getSelfUser())&&titleSetting.has(vc.getId()))
        ){
            //かつtitleが設定されている場合
            if (titleSetting.getJSONObject(vc.getId()).getBoolean("modified")){
                LibTitle.restoreTitle(event.getChannelLeft());
            }
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        User user = event.getMember().getUser();
        VoiceChannel channel = event.getChannelLeft();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        MultipleServer
            .getVCChannel(event.getGuild())
            .sendMessage(MessageFormat.format(":outbox_tray: `{0}` が <#{1}> から退出しました。",
                user.getName(),
                channel.getId()))
            .queue(
                message ->
                    new VoiceText().play(message,
                        MessageFormat.format("{0}が{1}から退出しました。",
                            user.getName(),
                            MsgFormatter.formatChannelName(channel)))
            );
    }
}
