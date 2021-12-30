package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * When someone leaves the VC, notify the VC text channel.
 */
public class Event_Disconnect extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (Main.getArgs().isDisableUserActivityNotify) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        AudioChannel vc = event.getChannelLeft();

        LibTitle libTitle = Main.getLibTitle();
        if (libTitle != null) {
            libTitle.processLeftTitle(vc);
        }

        User user = event.getMember().getUser();
        AudioChannel channel = event.getChannelLeft();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        MultipleServer
            .getVCChannel(event.getGuild())
            .sendMessage(":outbox_tray: `%s` が <#%s> から退出しました。".formatted(
                user.getName(),
                channel.getId()))
            .queue(
                message -> {
                    if (!event.getMember().getUser().isBot())
                        new VoiceText().play(
                            TrackInfo.SpeakFromType.QUITED_VC,
                            message,
                            "%sが%sから退出しました。".formatted(
                                user.getName(),
                                MsgFormatter.formatChannelName(channel))
                        );
                }
            );
    }
}
