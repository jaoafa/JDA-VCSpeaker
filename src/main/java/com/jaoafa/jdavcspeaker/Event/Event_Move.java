package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Event_Move extends ListenerAdapter {
    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (Main.getArgs().isDisableUserActivityNotify) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }

        VoiceChannel oldChannel = event.getOldValue();
        VoiceChannel newChannel = event.getNewValue();

        LibTitle libTitle = Main.getLibTitle();
        if (libTitle != null) {
            libTitle.processLeftTitle(oldChannel);
        }

        User user = event.getMember().getUser();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        MultipleServer
            .getVCChannel(event.getGuild())
            .sendMessage(":twisted_rightwards_arrows: `%s` が <#%s> から <#%s> に移動しました。".formatted(
                user.getName(),
                oldChannel.getId(),
                newChannel.getId()))
            .queue(
                message -> {
                    if (!event.getMember().getUser().isBot())
                        new VoiceText().play(
                            TrackInfo.SpeakFromType.MOVED_VC,
                            message,
                            "%sが%sから%sに移動しました。".formatted(
                                user.getName(),
                                MsgFormatter.formatChannelName(oldChannel),
                                MsgFormatter.formatChannelName(newChannel))
                        );
                }
            );
    }
}
