package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.MsgFormatter.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

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
            .sendMessage(MessageFormat.format(":twisted_rightwards_arrows: `{0}` が <#{1}> から <#{2}> に移動しました。",
                user.getName(),
                oldChannel.getId(),
                newChannel.getId()))
            .queue(
                message -> {
                    if (!event.getMember().getUser().isBot())
                        new VoiceText().play(
                            TrackInfo.SpeakFromType.MOVED_VC,
                            message,
                            MessageFormat.format("{0}が{1}から{2}に移動しました。",
                                user.getName(),
                                MsgFormatter.formatChannelName(oldChannel),
                                MsgFormatter.formatChannelName(newChannel))
                        );
                }
            );
    }
}
