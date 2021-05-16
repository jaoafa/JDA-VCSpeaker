package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibJson;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

public class Event_Title extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        process(event.getGuild(), event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        process(event.getGuild(), event.getChannelLeft());
    }

    void process(Guild guild, VoiceChannel vc) {
        if (!MultipleServer.isTargetServer(guild)) {
            return;
        }
        JSONObject titleSetting = LibJson.readObject("./title.json");
        if (vc.getMembers().size() != 0) {
            return;
        }
        if (!titleSetting.has(vc.getId())) {
            return;
        }
        if (!titleSetting.getJSONObject(vc.getId()).getBoolean("modified")) {
            return;
        }
        LibTitle.restoreTitle(vc);
    }
}
