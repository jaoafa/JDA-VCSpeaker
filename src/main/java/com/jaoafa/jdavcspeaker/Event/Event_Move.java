package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.MsgFormatter;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

public class Event_Move extends ListenerAdapter {
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        User user = event.getMember().getUser();
        VoiceChannel oldChannel = event.getOldValue();
        VoiceChannel newChannel = event.getNewValue();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        MultipleServer.getVCChannel(event.getGuild()).sendMessage(MessageFormat.format(":twisted_rightwards_arrows: `{0}` が <#{1}> から <#{2}> に移動しました。", user.getName(), oldChannel.getId(), newChannel.getId())).queue(
            message -> VoiceText.speak(message, MessageFormat.format("{0}が{1}から{2}に移動しました。",
                user.getName(),
                MsgFormatter.formatChannelName(oldChannel),
                MsgFormatter.formatChannelName(newChannel)))
        );
    }
}
