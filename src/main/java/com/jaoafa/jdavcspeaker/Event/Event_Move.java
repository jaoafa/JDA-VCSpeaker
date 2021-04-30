package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

public class Event_Move extends ListenerAdapter {
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        User user = event.getMember().getUser();
        VoiceChannel oldChannel = event.getOldValue();
        VoiceChannel newChannel = event.getNewValue();
        if (StaticData.textChannel == null) return;
        StaticData.textChannel.sendMessage(MessageFormat.format(":twisted_rightwards_arrows: `{0}`が`{1}`から`{2}`に移動しました。", user.getName(), oldChannel.getName(), newChannel.getName())).queue(
            message -> VoiceText.speak(message, MessageFormat.format("{0}が{1}から{2}に移動しました。", user.getName(), oldChannel.getName(), newChannel.getName()))
        );
    }
}
