package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Event_Join extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (Main.getArgs().isDisableUserActivityNotify) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        User user = event.getMember().getUser();
        AudioChannel channel = event.getChannelJoined();
        if (channel == null) {
            return;
        }
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        MultipleServer
            .getVCChannel(event.getGuild())
            .sendMessage(":inbox_tray: `%s` が <#%s> に参加しました。".formatted(
                user.getName(),
                channel.getId()))
            .queue(
                message -> {
                    if (!event.getMember().getUser().isBot())
                        new VoiceText()
                            .play(
                                TrackInfo.SpeakFromType.JOINED_VC,
                                message,
                                "%s が %s に参加しました。".formatted(
                                    user.getName(),
                                    MsgFormatter.formatChannelName(channel.getName()))
                            );
                }
            );
    }
}
