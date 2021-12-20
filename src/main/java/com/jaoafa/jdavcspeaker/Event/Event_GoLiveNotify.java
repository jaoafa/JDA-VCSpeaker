package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Event_GoLiveNotify extends ListenerAdapter {
    @Override
    public void onGuildVoiceStream(@NotNull GuildVoiceStreamEvent event) {
        if (Main.getArgs().isDisableGoLiveNotify) {
            return;
        }
        boolean isStream = event.isStream();
        Member member = event.getMember();
        VoiceChannel channel = event.getVoiceState().getChannel();
        if (channel == null) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            return; // 自身がどのVCにも参加していない
        }
        boolean isSpeak = event.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong() == channel.getIdLong();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;

        String text = isStream ?
            ":satellite: `%s` が <#%s> でGoLiveを開始しました。".formatted(
                member.getUser().getName(),
                channel.getId()) :
            ":satellite: `%s` が <#%s> でGoLiveを終了しました。".formatted(
                member.getUser().getName(),
                channel.getId());

        String speakText = isStream ?
            "%sがGoLiveを開始しました。".formatted(
                member.getUser().getName()) :
            "%sがGoLiveを終了しました。".formatted(
                member.getUser().getName());

        MultipleServer
            .getVCChannel(event.getGuild())
            .sendMessage(text)
            .queue(
                message -> {
                    if (isSpeak) {
                        new VoiceText().play(
                            isStream ? TrackInfo.SpeakFromType.STARTED_GOLIVE : TrackInfo.SpeakFromType.ENDED_GOLIVE,
                            message,
                            speakText
                        );
                    }
                }
            );
    }
}
