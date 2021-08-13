package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

public class Event_GoLiveNotify extends ListenerAdapter {
    @Override
    public void onGuildVoiceStream(GuildVoiceStreamEvent event) {
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
            MessageFormat.format(":satellite: `{0}` が <#{1}> でGoLiveを開始しました。",
                member.getUser().getName(),
                channel.getId()) :
            MessageFormat.format(":satellite: `{0}` が <#{1}> でGoLiveを終了しました。",
                member.getUser().getName(),
                channel.getId());

        String speakText = isStream ?
            MessageFormat.format("{0}がGoLiveを開始しました。",
                member.getUser().getName()) :
            MessageFormat.format("{0}がGoLiveを終了しました。",
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
