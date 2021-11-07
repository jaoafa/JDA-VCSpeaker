package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

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
        VoiceChannel vc = event.getChannelLeft();

        LibTitle libTitle = Main.getLibTitle();
        if (libTitle != null) {
            libTitle.processLeftTitle(vc);
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
                message -> {
                    if (!event.getMember().getUser().isBot())
                        new VoiceText().play(
                            TrackInfo.SpeakFromType.QUITED_VC,
                            message,
                            MessageFormat.format("{0}が{1}から退出しました。",
                                user.getName(),
                                MsgFormatter.formatChannelName(channel))
                        );
                }
            );
    }
}
